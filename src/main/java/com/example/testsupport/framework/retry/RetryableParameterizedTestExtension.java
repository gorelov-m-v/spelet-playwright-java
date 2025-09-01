package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Spliterator;
import java.util.Spliterators;

public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider,
        BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    private static final ExtensionContext.Namespace CONFIG = ExtensionContext.Namespace.create("retry", "config");
    private static final ExtensionContext.Namespace RUN = ExtensionContext.Namespace.create("retry", "run");

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getRequiredTestMethod().isAnnotationPresent(RetryableParameterizedTest.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        RetryableParameterizedTest annotation = context.getRequiredTestMethod().getAnnotation(RetryableParameterizedTest.class);
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

        List<Object[]> argumentsList = resolveArguments(context);
        Map<Integer, CopyOnWriteArrayList<Throwable>> histories = new ConcurrentHashMap<>();
        getRunStore(context).put("histories", histories);

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new ParameterizedTestIterator(context, argumentsList), Spliterator.NONNULL),
                false);
    }

    private ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.Namespace.create(CONFIG, context.getRequiredTestMethod()));
    }

    private ExtensionContext.Store getRunStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(RUN, context.getRequiredTestMethod(), context.getDisplayName()));
    }

    private List<Object[]> resolveArguments(ExtensionContext context) {
        List<Object[]> args = new ArrayList<>();
        List<ArgumentsSource> sources = Arrays.asList(context.getRequiredTestMethod().getAnnotationsByType(ArgumentsSource.class));
        for (ArgumentsSource source : sources) {
            try {
                ArgumentsProvider provider = source.value().getDeclaredConstructor().newInstance();
                Stream<? extends Arguments> stream = provider.provideArguments(context);
                args.addAll(stream.map(Arguments::get).collect(Collectors.toList()));
            } catch (Exception e) {
                throw new ExtensionConfigurationException("Failed to create ArgumentsProvider", e);
            }
        }
        return args;
    }

    private class ParameterizedTestIterator implements java.util.Iterator<TestTemplateInvocationContext> {
        private final ExtensionContext context;
        private final List<Object[]> argumentsList;
        private int currentScenario = 0;

        ParameterizedTestIterator(ExtensionContext context, List<Object[]> argumentsList) {
            this.context = context;
            this.argumentsList = argumentsList;
        }

        @Override
        public boolean hasNext() {
            Map<Integer, CopyOnWriteArrayList<Throwable>> histories = getRunStore(context).get("histories", Map.class);
            int totalRuns = getConfigStore(context).get("totalRuns", Integer.class);
            while (currentScenario < argumentsList.size()) {
                CopyOnWriteArrayList<Throwable> history = histories.computeIfAbsent(currentScenario, k -> new CopyOnWriteArrayList<>());
                if (history.isEmpty()) {
                    return true;
                }
                Throwable last = history.get(history.size() - 1);
                if (last == null) {
                    currentScenario++;
                    continue;
                }
                if (history.size() < totalRuns) {
                    return true;
                }
                currentScenario++;
            }
            return false;
        }

        @Override
        public TestTemplateInvocationContext next() {
            Map<Integer, CopyOnWriteArrayList<Throwable>> histories = getRunStore(context).get("histories", Map.class);
            CopyOnWriteArrayList<Throwable> history = histories.computeIfAbsent(currentScenario, k -> new CopyOnWriteArrayList<>());
            int attempt = history.size() + 1;
            getRunStore(context).put("currentScenario", currentScenario);
            getRunStore(context).put("currentAttempt", attempt);
            Object[] args = argumentsList.get(currentScenario);
            String displayName = String.format("[%d] attempt #%d", currentScenario + 1, attempt);
            return new TestTemplateInvocationContext() {
                @Override
                public String getDisplayName(int invocationIndex) {
                    return displayName;
                }

                @Override
                public List<Extension> getAdditionalExtensions() {
                    return List.of(new ParameterResolver() {
                        @Override
                        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                            return parameterContext.getIndex() < args.length;
                        }

                        @Override
                        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                            return args[parameterContext.getIndex()];
                        }
                    });
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
        Map<Integer, CopyOnWriteArrayList<Throwable>> histories = getRunStore(context).get("histories", Map.class);
        Integer scenario = getRunStore(context).get("currentScenario", Integer.class);
        CopyOnWriteArrayList<Throwable> history = histories.get(scenario);
        Class<? extends Throwable>[] retryable = getConfigStore(context).get("retryExceptions", Class[].class);
        boolean retryableException = isExceptionRetryable(throwable, retryable);
        if (!retryableException || history.size() + 1 >= totalRuns) {
            throw throwable;
        }
        getRunStore(context).put("lastException", throwable);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Map<Integer, CopyOnWriteArrayList<Throwable>> histories = getRunStore(context).get("histories", Map.class);
        Integer scenario = getRunStore(context).get("currentScenario", Integer.class);
        CopyOnWriteArrayList<Throwable> history = histories.get(scenario);
        Throwable last = getRunStore(context).remove("lastException", Throwable.class);
        history.add(last);
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
