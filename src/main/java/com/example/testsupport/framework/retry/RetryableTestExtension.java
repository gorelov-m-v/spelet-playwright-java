package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RetryableTestExtension implements TestTemplateInvocationContextProvider,
        BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    private static class RetryConfig {
        int repeats; // number of retries
        int minSuccess;
        long suspend;
        Class<? extends Throwable>[] exceptions;
    }

    private ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    private ExtensionContext.Store getRunStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context.getDisplayName()));
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().isPresent();
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        RetryableTest annotation = method.getAnnotation(RetryableTest.class);
        RetryConfig cfg = new RetryConfig();
        cfg.repeats = annotation.repeats();
        cfg.minSuccess = annotation.minSuccess();
        cfg.suspend = annotation.suspend();
        cfg.exceptions = annotation.exceptions();
        getConfigStore(context).put("config", cfg);

        TestTemplateIterator iterator = new TestTemplateIterator(context, cfg);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        RetryConfig cfg = getConfigStore(context).get("config", RetryConfig.class);
        Class<? extends Throwable>[] base = cfg.exceptions;
        Class<? extends Throwable>[] ext = Arrays.copyOf(base, base.length + 1);
        ext[base.length] = TestAbortedException.class;
        cfg.exceptions = ext;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ExtensionContext.Store store = getRunStore(context);
        @SuppressWarnings("unchecked")
        List<Boolean> history = (List<Boolean>) store.getOrComputeIfAbsent("history", key -> new ArrayList<>());
        history.add(context.getExecutionException().isPresent());
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        RetryConfig cfg = getConfigStore(context).get("config", RetryConfig.class);
        ExtensionContext.Store store = getRunStore(context);
        @SuppressWarnings("unchecked")
        List<Boolean> history = (List<Boolean>) store.getOrComputeIfAbsent("history", key -> new ArrayList<>());
        if (isExceptionRetryable(throwable, cfg.exceptions) && history.size() < cfg.repeats) {
            if (cfg.suspend > 0L) {
                TimeUnit.MILLISECONDS.sleep(cfg.suspend);
            }
            throw new TestAbortedException("Retrying test", throwable);
        }
        throw throwable;
    }

    private boolean isExceptionRetryable(Throwable throwable, Class<? extends Throwable>[] exceptions) {
        for (Class<? extends Throwable> ex : exceptions) {
            if (ex.isInstance(throwable)) {
                return true;
            }
        }
        Throwable cause = throwable.getCause();
        return cause != null && isExceptionRetryable(cause, exceptions);
    }

    private static class SimpleInvocationContext implements TestTemplateInvocationContext {
        private final String displayName;

        SimpleInvocationContext(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return displayName;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return Collections.emptyList();
        }
    }

    private class TestTemplateIterator implements Iterator<TestTemplateInvocationContext> {
        private final ExtensionContext context;
        private final RetryConfig cfg;
        private final String baseName;

        TestTemplateIterator(ExtensionContext context, RetryConfig cfg) {
            this.context = context;
            this.cfg = cfg;
            this.baseName = context.getDisplayName();
        }

        @Override
        public boolean hasNext() {
            ExtensionContext.Store store = getRunStore(context);
            @SuppressWarnings("unchecked")
            List<Boolean> history = (List<Boolean>) store.getOrComputeIfAbsent("history", key -> new ArrayList<>());
            if (history.isEmpty()) {
                return true; // first run
            }
            boolean lastFailed = history.get(history.size() - 1);
            return lastFailed && history.size() <= cfg.repeats;
        }

        @Override
        public TestTemplateInvocationContext next() {
            ExtensionContext.Store store = getRunStore(context);
            @SuppressWarnings("unchecked")
            List<Boolean> history = (List<Boolean>) store.getOrComputeIfAbsent("history", key -> new ArrayList<>());
            int attempt = history.size() + 1;
            int total = cfg.repeats + 1;
            String name = baseName;
            if (attempt > 1) {
                name = String.format("%s (retry %d/%d)", baseName, attempt - 1, total - 1);
            }
            return new SimpleInvocationContext(name);
        }
    }
}
