package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stub extension for {@link RetryableParameterizedTest}. It executes each set of
 * parameters exactly once without any retry logic.
 */
public class RetryableParameterizedTestExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        RetryableParameterizedTest annotation = testMethod.getAnnotation(RetryableParameterizedTest.class);
        ArgumentsProvider provider;
        try {
            provider = annotation.source().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate arguments provider", e);
        }
        Stream<? extends Arguments> argsStream;
        try {
            argsStream = provider.provideArguments(context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain arguments", e);
        }
        AtomicInteger index = new AtomicInteger(0);
        return argsStream.map(arguments -> createInvocationContext(arguments, annotation.name(), index.getAndIncrement()));
    }

    private TestTemplateInvocationContext createInvocationContext(Arguments arguments, String namePattern, int index) {
        Object[] args = arguments.get();
        String argumentsString = Arrays.stream(args).map(String::valueOf).collect(Collectors.joining(", "));
        String displayName = namePattern.replace("{index}", String.valueOf(index)).replace("{arguments}", argumentsString);
        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return displayName;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return Collections.singletonList(new ParameterResolver() {
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
