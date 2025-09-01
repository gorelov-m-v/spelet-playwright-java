package tests.framework;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/**
 * Supplies arguments for {@link RetryLogicTest#parameterizedRetryTest(String, int)}
 * covering various success and failure scenarios.
 */
public class FlakyTestArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of("immediate-success", 0),
                Arguments.of("success-after-one-failure", 1),
                Arguments.of("success-after-two-failures", 2),
                Arguments.of("always-fails", -1)
        );
    }
}

