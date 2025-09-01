package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.api.DisplayName;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

class RetryExtensionTest {

    private static final AtomicInteger flakyCounter = new AtomicInteger();

    @RetryableTest(repeats = 2)
    @DisplayName("Flaky test succeeds on second attempt")
    void flakyTestPassesAfterRetry() {
        if (flakyCounter.getAndIncrement() == 0) {
            Assertions.fail("Failing first attempt");
        }
    }

    static class ParamProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(org.junit.jupiter.api.extension.ExtensionContext context) {
            return Stream.of(
                    Arguments.of("alpha"),
                    Arguments.of("beta")
            );
        }
    }

    private static final ConcurrentHashMap<String, AtomicInteger> paramCounters = new ConcurrentHashMap<>();

    @RetryableParameterizedTest(repeats = 2)
    @ArgumentsSource(ParamProvider.class)
    @DisplayName("Parameterized flaky test succeeds after retry")
    void parameterizedFlakyTestPassesAfterRetry(String key) {
        AtomicInteger counter = paramCounters.computeIfAbsent(key, k -> new AtomicInteger());
        if (counter.getAndIncrement() == 0) {
            Assertions.fail("Failing first attempt for " + key);
        }
    }
}
