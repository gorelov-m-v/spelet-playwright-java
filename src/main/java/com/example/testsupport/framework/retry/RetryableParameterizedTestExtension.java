package com.example.testsupport.framework.retry;

import java.lang.reflect.Method;
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

/**
 * Placeholder extension for {@link RetryableParameterizedTest}.
 * <p>
 * It invokes the underlying test once for each set of arguments provided by
 * the configured {@link ArgumentsProvider}. No retry logic is performed at this
 * stage.
 */
public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().isPresent();
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        RetryableParameterizedTest annotation = testMethod.getAnnotation(RetryableParameterizedTest.class);
        try {
            ArgumentsProvider provider = annotation.source().getDeclaredConstructor().newInstance();
            Stream<? extends Arguments> args = provider.provideArguments(context);
            return args.map(arguments -> new TestTemplateInvocationContext() {
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
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to provide arguments", e);
        }
    }
}
