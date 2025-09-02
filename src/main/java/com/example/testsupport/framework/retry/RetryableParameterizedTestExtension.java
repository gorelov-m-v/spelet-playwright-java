package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.support.AnnotationSupport;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().isPresent() &&
                context.getTestMethod().get().isAnnotationPresent(RetryableParameterizedTest.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        RetryableParameterizedTest annotation = method.getAnnotation(RetryableParameterizedTest.class);
        RetryConfig config = new RetryConfig(annotation.repeats(), annotation.minSuccess(), annotation.suspend(), annotation.exceptions(), annotation.name(), annotation.repeatedName());
        getConfigStore(context).put("config", config);

        List<Arguments> argumentsList = resolveArguments(context);
        int totalRepeats = annotation.repeats();
        List<TestTemplateInvocationContext> contexts = new ArrayList<>();
        for (int index = 0; index < argumentsList.size(); index++) {
            Arguments args = argumentsList.get(index);
            String baseName = formatDisplayName(config.name(), index, args);
            for (int attempt = 1; attempt <= totalRepeats; attempt++) {
                contexts.add(new ParamInvocationContext(baseName, args, index, attempt, totalRepeats));
            }
        }
        return contexts.stream();
    }

    private List<Arguments> resolveArguments(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        return AnnotationSupport.findRepeatableAnnotations(method, org.junit.jupiter.params.provider.ArgumentsSource.class)
                .stream()
                .flatMap(source -> {
                    try {
                        ArgumentsProvider provider = source.value().getDeclaredConstructor().newInstance();
                        if (provider instanceof AnnotationConsumer<?> consumer) {
                            @SuppressWarnings("unchecked")
                            AnnotationConsumer<org.junit.jupiter.params.provider.ArgumentsSource> ac = (AnnotationConsumer<org.junit.jupiter.params.provider.ArgumentsSource>) consumer;
                            ac.accept(source);
                        }
                        return provider.provideArguments(context);
                    } catch (Exception e) {
                        throw new ExtensionConfigurationException("Failed to instantiate arguments provider", e);
                    }
                })
                .collect(Collectors.toList());
    }

    private String formatDisplayName(String pattern, int index, Arguments args) {
        Object[] argArray = args.get();
        String arguments = Stream.of(argArray).map(String::valueOf).collect(Collectors.joining(", "));
        return pattern.replace("{index}", String.valueOf(index)).replace("{arguments}", arguments);
    }

    private static ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(RetryableParameterizedTestExtension.class, context.getRequiredTestMethod()));
    }

    private static ExtensionContext.Store getRunStore(ExtensionContext context, String key) {
        return context.getStore(ExtensionContext.Namespace.create(RetryableParameterizedTestExtension.class, key));
    }

    private static class ParamInvocationContext implements TestTemplateInvocationContext {
        private final String baseName;
        private final Arguments arguments;
        private final int index;
        private final int attempt;
        private final int total;

        ParamInvocationContext(String baseName, Arguments arguments, int index, int attempt, int total) {
            this.baseName = baseName;
            this.arguments = arguments;
            this.index = index;
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
            List<Extension> extensions = new ArrayList<>();
            extensions.add(new ParameterResolver() {
                @Override
                public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                    return true;
                }

                @Override
                public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                    return arguments.get()[parameterContext.getIndex()];
                }
            });
            extensions.add(new RetryExtension(baseName, attempt));
            return extensions;
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

    private record RetryConfig(int repeats, int minSuccess, long suspend, Class<? extends Throwable>[] exceptions, String name, String repeatedName) {}
}
