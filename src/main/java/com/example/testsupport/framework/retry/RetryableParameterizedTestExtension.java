package com.example.testsupport.framework.retry;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().map(m -> m.isAnnotationPresent(RetryableParameterizedTest.class)).orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        RetryableParameterizedTest annotation = context.getRequiredTestMethod().getAnnotation(RetryableParameterizedTest.class);
        ArgumentsProvider provider;
        try {
            provider = annotation.source().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to instantiate ArgumentsProvider", e);
        }
        Stream<? extends Arguments> arguments;
        try {
            arguments = provider.provideArguments(context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to provide arguments", e);
        }
        return arguments.map(ParameterizedInvocationContext::new);
    }

    private static class ParameterizedInvocationContext implements TestTemplateInvocationContext {
        private final Arguments arguments;

        ParameterizedInvocationContext(Arguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return String.format("[%d]", invocationIndex);
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return Collections.singletonList(new ParameterResolver() {
                @Override
                public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                    Object[] args = arguments.get();
                    return parameterContext.getIndex() < args.length;
                }

                @Override
                public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                    return arguments.get()[parameterContext.getIndex()];
                }
            });
        }
    }
}
