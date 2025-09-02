package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;
import org.opentest4j.TestAbortedException;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Extension implementing retry logic for simple tests.
 */
public class RetryableTestExtension implements TestTemplateInvocationContextProvider,
        BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    private static final String CONFIG_KEY = "config";
    private static final String HISTORY_KEY = "history";

    private ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    private ExtensionContext.Store getRunStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getRequiredTestMethod().isAnnotationPresent(RetryableTest.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        RetryableTest annotation = context.getRequiredTestMethod().getAnnotation(RetryableTest.class);
        Config config = new Config(annotation.repeats(), annotation.minSuccess(), annotation.suspend(), annotation.exceptions());
        getConfigStore(context).put(CONFIG_KEY, config);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TestIterator(context, config), 0), false);
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

    private class TestIterator implements Iterator<TestTemplateInvocationContext> {
        private final ExtensionContext methodContext;
        private final Config config;

        TestIterator(ExtensionContext methodContext, Config config) {
            this.methodContext = methodContext;
            this.config = config;
        }

        @Override
        public boolean hasNext() {
            ExtensionContext.Store store = getRunStore(methodContext);
            @SuppressWarnings("unchecked")
            List<Boolean> history = (List<Boolean>) store.get(HISTORY_KEY);
            if (history == null || history.isEmpty()) {
                return true;
            }
            boolean lastFailed = history.get(history.size() - 1);
            return lastFailed && history.size() <= config.repeats();
        }

        @Override
        public TestTemplateInvocationContext next() {
            ExtensionContext.Store store = getRunStore(methodContext);
            @SuppressWarnings("unchecked")
            List<Boolean> history = (List<Boolean>) store.get(HISTORY_KEY);
            int current = (history == null ? 0 : history.size()) + 1;
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
                    return methodContext.getDisplayName();
                }
            };
        }
    }
}
