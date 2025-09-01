package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Execution(ExecutionMode.CONCURRENT)
class RetryLogicTest {

    private static int flakyCounter = 0;

    @RetryableTest(repeats = 2)
    @DisplayName("Flaky test succeeds after retry")
    void whenTestIsFlaky_thenItShouldSucceedAfterRetry() {
        int current = ++flakyCounter;
        if (current == 1) {
            Assertions.fail("Failed on first attempt");
        }
    }

    @RetryableTest(repeats = 2)
    @DisplayName("Always failing test remains failed")
    void whenTestAlwaysFails_thenItShouldRemainFailed() {
        Assertions.fail("Always fails");
    }

    private static final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @RetryableParameterizedTest(repeats = 3)
    @ArgumentsSource(FlakyTestArgumentProvider.class)
    @DisplayName("Parameterized flaky scenarios")
    void whenParameterizedFlaky_thenEachScenarioBehavesCorrectly(String scenario) {
        int attempt = counters.computeIfAbsent(scenario, k -> new AtomicInteger()).incrementAndGet();
        switch (scenario) {
            case "success" -> {
                // pass
            }
            case "failOnce" -> {
                if (attempt < 2) {
                    Assertions.fail("Failing once");
                }
            }
            case "failTwice" -> {
                if (attempt < 3) {
                    Assertions.fail("Failing twice");
                }
            }
            case "alwaysFail" -> {
                Assertions.fail("Always fails");
            }
            default -> throw new IllegalArgumentException("Unknown scenario" + scenario);
        }
    }
}
