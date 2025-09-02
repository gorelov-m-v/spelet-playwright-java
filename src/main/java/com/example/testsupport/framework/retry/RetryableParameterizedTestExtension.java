package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import org.opentest4j.TestAbortedException;

import java.util.stream.IntStream;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Extension implementing retry logic for parameterized tests.
 */
public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider,
        BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    private static final String CONFIG_KEY = "config";
    private static final String HISTORY_KEY = "history";

    // --- Utility store methods ------------------------------------------------------------
    private ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    private ExtensionContext.Store getRunStore(ExtensionContext context, int index) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod(), index));
    }

    private ExtensionContext.Store getRunStore(ExtensionContext context) {
        int index = extractIndex(context.getDisplayName());
        return getRunStore(context, index);
    }

    private int extractIndex(String displayName) {
        Matcher m = Pattern.compile("^\\[(\\d+)\\]").matcher(displayName);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    // -------------------------------------------------------------------------------------
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getRequiredTestMethod().isAnnotationPresent(RetryableParameterizedTest.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        RetryableParameterizedTest annotation = method.getAnnotation(RetryableParameterizedTest.class);

        Config config = new Config(annotation.repeats(), annotation.minSuccess(), annotation.suspend(), annotation.exceptions());
        getConfigStore(context).put(CONFIG_KEY, config);

        // Collect arguments from all @ArgumentsSource annotations
        List<Arguments> arguments = new ArrayList<>();
        for (ArgumentsSource src : method.getAnnotationsByType(ArgumentsSource.class)) {
            try {
                ArgumentsProvider provider = src.value().getDeclaredConstructor().newInstance();
                provider.provideArguments(context).forEach(arguments::add);
            } catch (Exception e) {
                throw new ExtensionConfigurationException("Failed to instantiate ArgumentsProvider", e);
            }
        }

        // Build dynamic stream for each argument set
        return IntStream.range(0, arguments.size())
                .mapToObj(index -> StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                new TestTemplateIterator(context, index, arguments.get(index).get(), annotation, config),
                                Spliterator.ORDERED),
                        false))
                .flatMap(s -> s);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Config config = getConfigStore(context).get(CONFIG_KEY, Config.class);
        if (config != null && Arrays.stream(config.exceptions()).noneMatch(TestAbortedException.class::equals)) {
            Class<? extends Throwable>[] ex = Arrays.copyOf(config.exceptions(), config.exceptions().length + 1);
            ex[ex.length - 1] = TestAbortedException.class;
            config = new Config(config.repeats(), config.minSuccess(), config.suspend(), ex);
            getConfigStore(context).put(CONFIG_KEY, config);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ExtensionContext.Store store = getRunStore(context);
        @SuppressWarnings("unchecked")
        List<Boolean> history = (List<Boolean>) store.get(HISTORY_KEY);
        if (history == null) {
            history = new ArrayList<>();
            store.put(HISTORY_KEY, history);
        }
        history.add(context.getExecutionException().isPresent());
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        Config config = getConfigStore(context).get(CONFIG_KEY, Config.class);
        ExtensionContext.Store runStore = getRunStore(context);
        @SuppressWarnings("unchecked")
        List<Boolean> history = (List<Boolean>) runStore.get(HISTORY_KEY);
        if (history == null) {
            history = new ArrayList<>();
        }
        if (isExceptionRetryable(throwable, config.exceptions()) && history.size() < config.repeats()) {
            throw new TestAbortedException("Retrying due to exception", throwable);
        } else {
            throw throwable;
        }
    }

    private boolean isExceptionRetryable(Throwable throwable, Class<? extends Throwable>[] retryables) {
        for (Class<? extends Throwable> clazz : retryables) {
            if (clazz.isInstance(throwable)) {
                return true;
            }
        }
        Throwable cause = throwable.getCause();
        return cause != null && isExceptionRetryable(cause, retryables);
    }

    // -------------------------------------------------------------------------------------
    private static class Config {
        private final int repeats;
        private final int minSuccess;
        private final long suspend;
        private final Class<? extends Throwable>[] exceptions;

        Config(int repeats, int minSuccess, long suspend, Class<? extends Throwable>[] exceptions) {
            this.repeats = repeats;
            this.minSuccess = minSuccess;
            this.suspend = suspend;
            this.exceptions = exceptions;
        }

        int repeats() { return repeats; }
        int minSuccess() { return minSuccess; }
        long suspend() { return suspend; }
        Class<? extends Throwable>[] exceptions() { return exceptions; }
    }

    private class TestTemplateIterator implements Iterator<TestTemplateInvocationContext> {
        private final ExtensionContext methodContext;
        private final int index;
        private final Object[] arguments;
        private final RetryableParameterizedTest annotation;
        private final Config config;
        private final String baseDisplayName;

        TestTemplateIterator(ExtensionContext methodContext, int index, Object[] arguments,
                              RetryableParameterizedTest annotation, Config config) {
            this.methodContext = methodContext;
            this.index = index;
            this.arguments = arguments;
            this.annotation = annotation;
            this.config = config;
            this.baseDisplayName = annotation.name()
                    .replace("{index}", Integer.toString(index))
                    .replace("{arguments}", Arrays.stream(arguments)
                            .map(Objects::toString)
                            .collect(Collectors.joining(", ")));
        }

        @Override
        public boolean hasNext() {
            ExtensionContext.Store store = getRunStore(methodContext, index);
            @SuppressWarnings("unchecked")
            List<Boolean> history = (List<Boolean>) store.get(HISTORY_KEY);
            if (history == null || history.isEmpty()) {
                return true; // initial run
            }
            boolean lastFailed = history.get(history.size() - 1);
            return lastFailed && history.size() <= config.repeats();
        }

        @Override
        public TestTemplateInvocationContext next() {
            ExtensionContext.Store store = getRunStore(methodContext, index);
            @SuppressWarnings("unchecked")
            List<Boolean> history = (List<Boolean>) store.get(HISTORY_KEY);
            int current = (history == null ? 0 : history.size()) + 1;
            int total = config.repeats() + 1;
            String name;
            if (current == 1) {
                name = baseDisplayName;
            } else {
                name = annotation.repeatedName()
                        .replace("{displayName}", baseDisplayName)
                        .replace("{currentRepetition}", Integer.toString(current))
                        .replace("{totalRepetitions}", Integer.toString(total));
            }
            if (current > 1 && config.suspend() > 0) {
                try {
                    Thread.sleep(config.suspend());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return new TestTemplateInvocationContext() {
                @Override
                public String getDisplayName(int invocationIndex) {
                    return name;
                }

                @Override
                public List<Extension> getAdditionalExtensions() {
                    return List.of(new ParameterResolver() {
                        @Override
                        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                            return parameterContext.getIndex() < arguments.length;
                        }

                        @Override
                        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                            return arguments[parameterContext.getIndex()];
                        }
                    });
                }
            };
        }
    }
}
