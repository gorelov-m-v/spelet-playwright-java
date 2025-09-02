package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Stub extension for {@link RetryableTest}. At this stage it simply runs the
 * underlying test once without any retry logic.
 */
public class RetryableTestExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return Stream.of(new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return context.getDisplayName();
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return Collections.emptyList();
            }
        });
    }
}
