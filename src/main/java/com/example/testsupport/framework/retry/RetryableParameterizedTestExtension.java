package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * Extension providing retry logic for parameterized tests.
 */
public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider {

    private static final ExtensionContext.Namespace CONFIG_NS = ExtensionContext.Namespace.create(RetryableParameterizedTestExtension.class, "CONFIG");
    private static final ExtensionContext.Namespace RUN_NS = ExtensionContext.Namespace.create(RetryableParameterizedTestExtension.class, "RUN");

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().map(m -> m.isAnnotationPresent(RetryableParameterizedTest.class)).orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        RetryableParameterizedTest ann = method.getAnnotation(RetryableParameterizedTest.class);
        Config config = Config.from(ann);
        context.getStore(CONFIG_NS).put(method, config);
        ArgumentsProvider provider;
        try {
            provider = ann.source().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<Arguments> args;
        try {
            args = new ArrayList<>();
            provider.provideArguments(context).forEach(args::add);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return IntStream.range(0, args.size())
                .boxed()
                .flatMap(index -> StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(new ParamRetryIterator(context, config, index, args.get(index)), Spliterator.ORDERED),
                        false));
    }

    private record Config(int repeats, int minSuccess, long suspend, List<Class<? extends Throwable>> exceptions,
                          String name, String repeatedName) {
        static Config from(RetryableParameterizedTest ann) {
            int repeats = Integer.getInteger("retry.repeats", ann.repeats());
            int minSuccess = Integer.getInteger("retry.minSuccess", ann.minSuccess());
            long suspend = Long.getLong("retry.suspend", ann.suspend());
            String exProp = System.getProperty("retry.exceptions");
            List<Class<? extends Throwable>> exceptions;
            if (exProp != null && !exProp.isBlank()) {
                exceptions = Arrays.stream(exProp.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(RetryableParameterizedTestExtension::classForName)
                        .toList();
            } else {
                exceptions = List.of(ann.exceptions());
            }
            return new Config(repeats, minSuccess, suspend, exceptions, ann.name(), ann.repeatedName());
        }
    }

    private static Class<? extends Throwable> classForName(String name) {
        try {
            return Class.forName(name).asSubclass(Throwable.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static class RunState {
        final AtomicInteger attempts = new AtomicInteger();
        final AtomicInteger successes = new AtomicInteger();
        volatile boolean giveUp = false;
        volatile Throwable lastThrowable;
    }

    private static RunState getRunState(ExtensionContext ctx, String key) {
        return ctx.getStore(RUN_NS).getOrComputeIfAbsent(key, k -> new RunState(), RunState.class);
    }

    private static String formatDisplayName(Config config, String base, int index, Arguments args, int attempt) {
        String name = config.name()
                .replace("{index}", String.valueOf(index))
                .replace("{arguments}", Arrays.toString(args.get()) );
        String display = name;
        if (attempt > 1) {
            display = config.repeatedName()
                    .replace("{displayName}", name)
                    .replace("{currentRepetition}", String.valueOf(attempt - 1))
                    .replace("{totalRepetitions}", String.valueOf(config.repeats()));
        }
        return display;
    }

    private static class ParamRetryIterator implements Iterator<TestTemplateInvocationContext> {
        private final ExtensionContext parent;
        private final Config config;
        private final int index;
        private final Arguments arguments;
        private final String key;

        ParamRetryIterator(ExtensionContext parent, Config config, int index, Arguments arguments) {
            this.parent = parent;
            this.config = config;
            this.index = index;
            this.arguments = arguments;
            this.key = parent.getDisplayName() + "#" + index + Arrays.toString(arguments.get());
        }

        @Override
        public boolean hasNext() {
            RunState state = getRunState(parent, key);
            return state.attempts.get() < (config.repeats + 1)
                    && state.successes.get() < config.minSuccess
                    && !state.giveUp;
        }

        @Override
        public TestTemplateInvocationContext next() {
            RunState state = getRunState(parent, key);
            int attempt = state.attempts.incrementAndGet();
            String display = formatDisplayName(config, parent.getDisplayName(), index, arguments, attempt);
            return new ParamInvocationContext(display, config, state, arguments);
        }
    }

    private static class ParamInvocationContext implements TestTemplateInvocationContext, ParameterResolver, AfterTestExecutionCallback, TestExecutionExceptionHandler {
        private final String displayName;
        private final Config config;
        private final RunState state;
        private final Arguments arguments;

        ParamInvocationContext(String displayName, Config config, RunState state, Arguments arguments) {
            this.displayName = displayName;
            this.config = config;
            this.state = state;
            this.arguments = arguments;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return displayName;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return List.of(this);
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return true;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return arguments.get()[parameterContext.getIndex()];
        }

        @Override
        public void afterTestExecution(ExtensionContext context) {
            if (state.lastThrowable == null) {
                state.successes.incrementAndGet();
            }
            state.lastThrowable = null;
        }

        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
            state.lastThrowable = throwable;
            if (!isRetryable(throwable, config.exceptions)) {
                state.giveUp = true;
            } else if (config.suspend > 0) {
                try {
                    Thread.sleep(config.suspend);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            throw throwable;
        }
    }

    private static boolean isRetryable(Throwable throwable, List<Class<? extends Throwable>> allowed) {
        Throwable current = throwable;
        while (current != null) {
            for (Class<? extends Throwable> clazz : allowed) {
                if (clazz.isAssignableFrom(current.getClass())) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
