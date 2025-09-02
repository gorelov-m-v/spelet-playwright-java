package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;
import org.opentest4j.TestAbortedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RetryableTestExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().isPresent() &&
                context.getTestMethod().get().isAnnotationPresent(RetryableTest.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        RetryableTest annotation = context.getRequiredTestMethod().getAnnotation(RetryableTest.class);
        RetryConfig config = new RetryConfig(annotation.repeats(), annotation.minSuccess(), annotation.suspend(), annotation.exceptions());
        getConfigStore(context).put("config", config);
        int total = annotation.repeats();
        List<TestTemplateInvocationContext> contexts = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            int attempt = i;
            contexts.add(new RetryInvocationContext(context.getDisplayName(), attempt, total));
        }
        return contexts.stream();
    }

    private static ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(RetryableTestExtension.class, context.getRequiredTestMethod()));
    }

    private static ExtensionContext.Store getRunStore(ExtensionContext context, String key) {
        return context.getStore(ExtensionContext.Namespace.create(RetryableTestExtension.class, key));
    }

    private static class RetryInvocationContext implements TestTemplateInvocationContext {
        private final String baseName;
        private final int attempt;
        private final int total;

        RetryInvocationContext(String baseName, int attempt, int total) {
            this.baseName = baseName;
            this.attempt = attempt;
            this.total = total;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            if (attempt == 1) {
                return baseName;
            }
            return baseName + " (retry " + attempt + "/" + total + ")";
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return List.of(new RetryExtension(baseName, attempt));
        }
    }

    private static class RetryExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {
        private final String key;
        private final int attempt;

        RetryExtension(String key, int attempt) {
            this.key = key;
            this.attempt = attempt;
        }

        @Override
        public void beforeTestExecution(ExtensionContext context) throws Exception {
            List<Boolean> history = getRunStore(context, key).getOrComputeIfAbsent("history", k -> new ArrayList<>(), List.class);
            if (!history.isEmpty() && !history.get(history.size() - 1)) {
                throw new TestAbortedException("Test already passed");
            }
        }

        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
            RetryConfig config = getConfigStore(context).get("config", RetryConfig.class);
            List<Boolean> history = getRunStore(context, key).getOrComputeIfAbsent("history", k -> new ArrayList<>(), List.class);
            history.add(Boolean.TRUE);
            if (history.size() < config.repeats() && isExceptionRetryable(throwable, config.exceptions())) {
                throw new TestAbortedException("Retrying", throwable);
            } else {
                throw throwable;
            }
        }

        @Override
        public void afterTestExecution(ExtensionContext context) throws Exception {
            List<Boolean> history = getRunStore(context, key).getOrComputeIfAbsent("history", k -> new ArrayList<>(), List.class);
            if (history.size() < attempt) {
                history.add(Boolean.FALSE);
            }
        }
    }

    private static boolean isExceptionRetryable(Throwable ex, Class<? extends Throwable>[] types) {
        for (Class<? extends Throwable> type : types) {
            if (type.isInstance(ex)) {
                return true;
            }
        }
        Throwable cause = ex.getCause();
        return cause != null && isExceptionRetryable(cause, types);
    }

    private record RetryConfig(int repeats, int minSuccess, long suspend, Class<? extends Throwable>[] exceptions) {}
}
