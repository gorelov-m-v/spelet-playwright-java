package com.example.testsupport.framework.retry;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

/**
 * Placeholder extension for {@link RetryableTest}.
 * <p>
 * For the initial TDD stage this extension simply invokes the underlying test
 * exactly once without performing any retry logic. The full retry mechanism
 * will be implemented in subsequent stages.
 */
public class RetryableTestExtension implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().isPresent();
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return Stream.of(new TestTemplateInvocationContext() {});
    }
}
