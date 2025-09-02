package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider,
        BeforeTestExecutionCallback, AfterTestExecutionCallback, TestExecutionExceptionHandler {

    private static class RetryConfig {
        int repeats;
        int minSuccess;
        long suspend;
        Class<? extends Throwable>[] exceptions;
        String repeatedName;
    }

    private ExtensionContext.Store getConfigStore(ExtensionContext context) {
        return context.getStore(Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

    private ExtensionContext.Store getRunStore(ExtensionContext context, String key) {
        return context.getStore(Namespace.create(getClass(), key));
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().isPresent();
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        RetryableParameterizedTest annotation = method.getAnnotation(RetryableParameterizedTest.class);
        RetryConfig cfg = new RetryConfig();
        cfg.repeats = annotation.repeats();
        cfg.minSuccess = annotation.minSuccess();
        cfg.suspend = annotation.suspend();
        cfg.exceptions = annotation.exceptions();
        cfg.repeatedName = annotation.repeatedName();
        getConfigStore(context).put("config", cfg);

        List<Arguments> arguments = new ArrayList<>();
        for (ArgumentsSource src : method.getAnnotationsByType(ArgumentsSource.class)) {
            try {
                ArgumentsProvider provider = src.value().getDeclaredConstructor().newInstance();
                provider.provideArguments(context).forEach(arguments::add);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        AtomicInteger index = new AtomicInteger();
        return arguments.stream().flatMap(args -> {
            int idx = index.getAndIncrement();
            String baseName = formatName(annotation.name(), idx, args.get());
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new TestTemplateIterator(context, cfg, args.get(), baseName), 0), false);
        });
    }

    private String formatName(String pattern, int index, Object[] args) {
        String argsString = Arrays.stream(args).map(String::valueOf).collect(Collectors.joining(", "));
        return pattern.replace("{index}", String.valueOf(index)).replace("{arguments}", argsString);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        RetryConfig cfg = getConfigStore(context.getParent().orElse(context)).get("config", RetryConfig.class);
        Class<? extends Throwable>[] base = cfg.exceptions;
        Class<? extends Throwable>[] ext = Arrays.copyOf(base, base.length + 1);
        ext[base.length] = TestAbortedException.class;
        cfg.exceptions = ext;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ExtensionContext methodCtx = context.getParent().orElse(context);
        String baseName = methodCtx.getStore(Namespace.create(getClass(), "name-mapping"))
                .get(context.getDisplayName(), String.class);
        ExtensionContext.Store store = getRunStore(methodCtx, baseName);
        @SuppressWarnings("unchecked")
        List<Boolean> history = (List<Boolean>) store.getOrComputeIfAbsent("history", k -> new ArrayList<>());
        history.add(context.getExecutionException().isPresent());
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        ExtensionContext methodCtx = context.getParent().orElse(context);
        RetryConfig cfg = getConfigStore(methodCtx).get("config", RetryConfig.class);
        String baseName = methodCtx.getStore(Namespace.create(getClass(), "name-mapping"))
                .get(context.getDisplayName(), String.class);
        ExtensionContext.Store store = getRunStore(methodCtx, baseName);
        @SuppressWarnings("unchecked")
        List<Boolean> history = (List<Boolean>) store.getOrComputeIfAbsent("history", k -> new ArrayList<>());
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

    private class TestTemplateIterator implements Iterator<TestTemplateInvocationContext> {
        private final ExtensionContext methodContext;
        private final RetryConfig cfg;
        private final Object[] arguments;
        private final String baseName;

        TestTemplateIterator(ExtensionContext methodContext, RetryConfig cfg, Object[] arguments, String baseName) {
            this.methodContext = methodContext;
            this.cfg = cfg;
            this.arguments = arguments;
            this.baseName = baseName;
        }

        @Override
        public boolean hasNext() {
            ExtensionContext.Store store = getRunStore(methodContext, baseName);
            @SuppressWarnings("unchecked")
            List<Boolean> history = (List<Boolean>) store.getOrComputeIfAbsent("history", k -> new ArrayList<>());
            if (history.isEmpty()) {
                return true;
            }
            boolean lastFailed = history.get(history.size() - 1);
            return lastFailed && history.size() <= cfg.repeats;
        }

        @Override
        public TestTemplateInvocationContext next() {
            ExtensionContext.Store store = getRunStore(methodContext, baseName);
            @SuppressWarnings("unchecked")
            List<Boolean> history = (List<Boolean>) store.getOrComputeIfAbsent("history", k -> new ArrayList<>());
            int attempt = history.size() + 1;
            int total = cfg.repeats + 1;
            String name = baseName;
            if (attempt > 1) {
                name = cfg.repeatedName
                        .replace("{displayName}", baseName)
                        .replace("{currentRepetition}", String.valueOf(attempt - 1))
                        .replace("{totalRepetitions}", String.valueOf(total - 1));
            }
            methodContext.getStore(Namespace.create(getClass(), "name-mapping")).put(name, baseName);
            return new ParameterizedInvocationContext(arguments, name);
        }
    }

    private static class ParameterizedInvocationContext implements TestTemplateInvocationContext {
        private final Object[] arguments;
        private final String displayName;

        ParameterizedInvocationContext(Object[] arguments, String displayName) {
            this.arguments = arguments;
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return displayName;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            Extension resolver = new ParameterResolver() {
                @Override
                public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                    return parameterContext.getIndex() < arguments.length;
                }

                @Override
                public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                    return arguments[parameterContext.getIndex()];
                }
            };
            return Collections.singletonList(resolver);
        }
    }
}
