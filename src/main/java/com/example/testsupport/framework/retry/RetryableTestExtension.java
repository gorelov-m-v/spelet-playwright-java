package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * JUnit extension that adds retry logic for {@link RetryableTest}.
 */
public class RetryableTestExtension implements TestTemplateInvocationContextProvider {

    private static final ExtensionContext.Namespace CONFIG_NS = ExtensionContext.Namespace.create(RetryableTestExtension.class, "CONFIG");
    private static final ExtensionContext.Namespace RUN_NS = ExtensionContext.Namespace.create(RetryableTestExtension.class, "RUN");

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().map(m -> m.isAnnotationPresent(RetryableTest.class)).orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        RetryableTest annotation = method.getAnnotation(RetryableTest.class);
        Config config = Config.from(annotation);
        context.getStore(CONFIG_NS).put(method, config);
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new RetryIterator(context, config), Spliterator.ORDERED),
                false);
    }

    /** Configuration holder. */
    private record Config(int repeats, int minSuccess, long suspend, List<Class<? extends Throwable>> exceptions) {
        static Config from(RetryableTest ann) {
            int repeats = Integer.getInteger("retry.repeats", ann.repeats());
            int minSuccess = Integer.getInteger("retry.minSuccess", ann.minSuccess());
            long suspend = Long.getLong("retry.suspend", ann.suspend());
            String exProp = System.getProperty("retry.exceptions");
            List<Class<? extends Throwable>> exceptions;
            if (exProp != null && !exProp.isBlank()) {
                exceptions = Arrays.stream(exProp.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(RetryableTestExtension::classForName)
                        .toList();
            } else {
                exceptions = List.of(ann.exceptions());
            }
            return new Config(repeats, minSuccess, suspend, exceptions);
        }
    }

    private static Class<? extends Throwable> classForName(String name) {
        try {
            return Class.forName(name).asSubclass(Throwable.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** State holder per invocation. */
    private static class RunState {
        final AtomicInteger attempts = new AtomicInteger();
        final AtomicInteger successes = new AtomicInteger();
        volatile boolean giveUp = false;
        volatile Throwable lastThrowable;
    }

    private static RunState getRunState(ExtensionContext context) {
        return context.getStore(RUN_NS).getOrComputeIfAbsent(context.getUniqueId(), k -> new RunState(), RunState.class);
    }

    /** Iterator providing contexts until success or attempts exhausted. */
    private static class RetryIterator implements Iterator<TestTemplateInvocationContext> {
        private final ExtensionContext parent;
        private final Config config;

        RetryIterator(ExtensionContext parent, Config config) {
            this.parent = parent;
            this.config = config;
        }

        @Override
        public boolean hasNext() {
            RunState state = getRunState(parent);
            return state.attempts.get() < (config.repeats + 1)
                    && state.successes.get() < config.minSuccess
                    && !state.giveUp;
        }

        @Override
        public TestTemplateInvocationContext next() {
            RunState state = getRunState(parent);
            int attempt = state.attempts.incrementAndGet();
            String display = attempt == 1
                    ? parent.getDisplayName()
                    : parent.getDisplayName() + String.format(" (retry %d/%d)", attempt - 1, config.repeats);
            return new RetryInvocationContext(display, config, state);
        }
    }

    /** Invocation context wrapping execution callbacks. */
    private static class RetryInvocationContext implements TestTemplateInvocationContext, AfterTestExecutionCallback, TestExecutionExceptionHandler {
        private final String displayName;
        private final Config config;
        private final RunState state;

        RetryInvocationContext(String displayName, Config config, RunState state) {
            this.displayName = displayName;
            this.config = config;
            this.state = state;
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
        public void afterTestExecution(ExtensionContext context) {
            if (state.lastThrowable == null) {
                state.successes.incrementAndGet();
            }
            state.lastThrowable = null; // reset for next attempt
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
