package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().isPresent()
                && context.getRequiredTestMethod().isAnnotationPresent(RetryableParameterizedTest.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        RetryableParameterizedTest annotation = method.getAnnotation(RetryableParameterizedTest.class);

        List<Arguments> arguments = new ArrayList<>();
        for (ArgumentsSource source : AnnotationSupport.findRepeatableAnnotations(method, ArgumentsSource.class)) {
            try {
                ArgumentsProvider provider = source.value().getDeclaredConstructor().newInstance();
                provider.provideArguments(context).forEach(arguments::add);
            } catch (Exception e) {
                throw new ExtensionConfigurationException("Failed to instantiate ArgumentsProvider", e);
            }
        }

        AtomicInteger index = new AtomicInteger();
        return arguments.stream().map(args -> new TestTemplateInvocationContext() {
            final int currentIndex = index.getAndIncrement();

            @Override
            public String getDisplayName(int invocationIndex) {
                String name = annotation.name();
                String argsString = java.util.Arrays.toString(args.get());
                return name
                        .replace("{index}", String.valueOf(currentIndex))
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
                        return args.get()[parameterContext.getIndex()];
                    }
                });
            }
        });
    }
}
