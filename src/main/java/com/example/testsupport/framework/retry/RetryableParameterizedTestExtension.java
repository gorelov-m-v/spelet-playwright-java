package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        RetryableParameterizedTest annotation = testMethod.getAnnotation(RetryableParameterizedTest.class);
        Class<? extends ArgumentsProvider> providerClass = annotation.source();
        ArgumentsProvider provider;
        try {
            provider = providerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ExtensionConfigurationException("Failed to instantiate ArgumentsProvider", e);
        }
        Stream<? extends Arguments> arguments;
        try {
            arguments = provider.provideArguments(context);
        } catch (Exception e) {
            throw new ExtensionConfigurationException("Failed to provide arguments", e);
        }
        AtomicInteger index = new AtomicInteger(0);
        return arguments.map(args -> new SimpleInvocationContext(args, annotation.name(), index.getAndIncrement()));
    }

    private static class SimpleInvocationContext implements TestTemplateInvocationContext {
        private final Arguments arguments;
        private final String namePattern;
        private final int index;

        SimpleInvocationContext(Arguments arguments, String namePattern, int index) {
            this.arguments = arguments;
            this.namePattern = namePattern;
            this.index = index;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            String argsString = Arrays.toString(arguments.get());
            return namePattern
                    .replace("{index}", String.valueOf(index))
                    .replace("{arguments}", argsString);
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return List.of(new ParameterResolver() {
                @Override
                public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                    return true;
                }

                @Override
                public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
                    return arguments.get()[parameterContext.getIndex()];
                }
            });
        }
    }
}
