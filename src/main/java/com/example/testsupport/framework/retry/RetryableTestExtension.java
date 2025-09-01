package com.example.testsupport.framework.retry;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.util.ResultsUtils;
import org.junit.jupiter.api.extension.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RetryableTestExtension implements TestTemplateInvocationContextProvider,
        BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    private static final ExtensionContext.Namespace CONFIG = ExtensionContext.Namespace.create("retry", "config");
    private static final ExtensionContext.Namespace RUN = ExtensionContext.Namespace.create("retry", "run");

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getRequiredTestMethod().isAnnotationPresent(RetryableTest.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        RetryableTest annotation = context.getRequiredTestMethod().getAnnotation(RetryableTest.class);
        int repeats = annotation.repeats();
        String repeatsProp = System.getProperty("retry.repeats");
        if (repeatsProp != null) {
            try {
                repeats = Integer.parseInt(repeatsProp);
            } catch (NumberFormatException ignored) {
            }
        }
        getConfigStore(context).put("totalRuns", repeats);
        getConfigStore(context).put("retryExceptions", annotation.exceptions());
        getRunStore(context).put("history", new CopyOnWriteArrayList<Throwable>());
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new TestTemplateIterator(context), Spliterator.NONNULL), false);
    }

    private ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.Namespace.create(CONFIG, context.getRequiredTestMethod()));
    }

    private ExtensionContext.Store getRunStore(ExtensionContext context) {
        ExtensionContext methodContext = context.getParent()
                .filter(c -> c.getTestMethod().isPresent())
                .orElse(context);
        return methodContext.getStore(ExtensionContext.Namespace.create(RUN, methodContext.getDisplayName()));
    }

    private class TestTemplateIterator implements java.util.Iterator<TestTemplateInvocationContext> {
        private final ExtensionContext context;

        TestTemplateIterator(ExtensionContext context) {
            this.context = context;
        }

        @Override
        public boolean hasNext() {
            List<Throwable> history = getRunStore(context).get("history", List.class);
            int totalRuns = getConfigStore(context).get("totalRuns", Integer.class);
            if (history.isEmpty()) {
                return true;
            }
            Throwable last = history.get(history.size() - 1);
            return last != null && history.size() < totalRuns;
        }

        @Override
        public TestTemplateInvocationContext next() {
            List<Throwable> history = getRunStore(context).get("history", List.class);
            int attempt = history.size() + 1;
            getRunStore(context).put("currentAttempt", attempt);
            return new TestTemplateInvocationContext() {
                @Override
                public String getDisplayName(int invocationIndex) {
                    return "Attempt #" + attempt;
                }

                @Override
                public List<Extension> getAdditionalExtensions() {
                    return new ArrayList<>();
                }
            };
        }
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Integer attempt = getRunStore(context).get("currentAttempt", Integer.class);
        System.out.println("Attempt #" + attempt);
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        int totalRuns = getConfigStore(context).get("totalRuns", Integer.class);
        List<Throwable> history = getRunStore(context).get("history", List.class);
        Class<? extends Throwable>[] retryable = getConfigStore(context).get("retryExceptions", Class[].class);
        boolean retryableException = isExceptionRetryable(throwable, retryable);
        if (!retryableException || history.size() + 1 >= totalRuns) {
            throw throwable;
        }
        Allure.getLifecycle().updateTestCase(result -> {
            result.setStatus(ResultsUtils.getStatus(throwable).orElse(Status.BROKEN));
            result.setStatusDetails(new StatusDetails()
                    .setMessage(throwable.getMessage())
                    .setTrace(getStackTrace(throwable))
                    .setFlaky(true));
        });
        getRunStore(context).put("lastException", throwable);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        List<Throwable> history = getRunStore(context).get("history", List.class);
        Throwable last = getRunStore(context).remove("lastException", Throwable.class);
        history.add(last);
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private boolean isExceptionRetryable(Throwable throwable, Class<? extends Throwable>[] retryable) {
        if (throwable == null) {
            return false;
        }
        for (Class<? extends Throwable> clazz : retryable) {
            if (clazz.isInstance(throwable)) {
                return true;
            }
        }
        return isExceptionRetryable(throwable.getCause(), retryable);
    }
}
