package com.example.testsupport.framework.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Diagnostic test suite that specifies and verifies retry behaviour.
 * <p>
 * The tests are executed concurrently to immediately highlight potential
 * thread-safety issues within the retry mechanism.
 */
@Execution(ExecutionMode.CONCURRENT)
class RetryLogicTest {

    private static final Map<String, AtomicInteger> attemptCounters = new ConcurrentHashMap<>();

    @BeforeEach
    void resetCounters() {
        attemptCounters.clear();
    }

    @Test
    @DisplayName("Flaky test should eventually succeed")
    @RetryableTest(repeats = 2, exceptions = {AssertionError.class})
    void whenTestIsFlaky_itShouldSucceed() {
        int attempt = attemptCounters.computeIfAbsent("flaky_simple", k -> new AtomicInteger()).incrementAndGet();
        if (attempt < 2) {
            Assertions.fail("flaky failure");
        }
    }

    @Test
    @DisplayName("Always failing test should remain failed")
    @RetryableTest(repeats = 2, exceptions = {AssertionError.class})
    void whenTestAlwaysFails_itShouldRemainFailed() {
        AssertionError error = assertThrows(AssertionError.class, () -> {
            int attempt = attemptCounters
                    .computeIfAbsent("always_fails_simple", k -> new AtomicInteger())
                    .incrementAndGet();
            Assertions.fail("always fails" + attempt);
        });
        assertEquals(3, attemptCounters.get("always_fails_simple").get());
        throw error;
    }

    @DisplayName("Parameterized flaky scenarios are handled per case")
    @RetryableParameterizedTest(
            repeats = 3,
            source = FlakyTestArgumentProvider.class,
            exceptions = {AssertionError.class})
    void whenParameterizedIsFlaky_eachCaseIsHandledCorrectly(String scenario, int failures) {
        AtomicInteger counter = attemptCounters.computeIfAbsent(scenario, k -> new AtomicInteger());
        int attempt = counter.incrementAndGet();
        if (failures == -1) {
            Assertions.fail("always fails");
        } else if (attempt <= failures + 1) {
            Assertions.fail("flaky attempt " + attempt);
        }
    }

    @Test
    @DisplayName("Non-matching exception types should not trigger retries")
    @RetryableTest(repeats = 5, exceptions = {IOException.class})
    void whenExceptionTypeDoesNotMatch_itShouldNotRetry() {
        AssertionError error = assertThrows(AssertionError.class, () -> {
            int attempt = attemptCounters
                    .computeIfAbsent("exception_filter", k -> new AtomicInteger())
                    .incrementAndGet();
            Assertions.fail("unexpected assertion" + attempt);
        });
        assertEquals(1, attemptCounters.get("exception_filter").get());
        throw error;
    }
}
