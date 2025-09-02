package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Diagnostic test suite describing the expected behaviour of retry mechanism.
 * At this stage the extension logic is not implemented, so most tests are
 * expected to fail on their first attempt.
 */
@Execution(ExecutionMode.CONCURRENT)
class RetryLogicTest {

    private static final Map<String, AtomicInteger> attemptCounters = new ConcurrentHashMap<>();

    @BeforeEach
    void reset() {
        attemptCounters.clear();
    }

    @Test
    @DisplayName("Flaky test should eventually succeed")
    @RetryableTest(repeats = 2, exceptions = {AssertionError.class})
    void whenTestIsFlaky_itShouldSucceed() {
        int attempt = attemptCounters.computeIfAbsent("flaky_simple", k -> new AtomicInteger()).incrementAndGet();
        if (attempt < 2) {
            fail("Intentional failure to simulate flakiness");
        }
    }

    @Test
    @DisplayName("Always failing test should remain failed")
    @RetryableTest(repeats = 2, exceptions = {AssertionError.class})
    void whenTestAlwaysFails_itShouldRemainFailed() {
        AtomicInteger counter = attemptCounters.computeIfAbsent("always_fails_simple", k -> new AtomicInteger());
        assertThrows(AssertionError.class, () -> {
            counter.incrementAndGet();
            fail("Always failing");
        });
        assertEquals(3, counter.get());
    }

    @DisplayName("Flaky parameterized scenarios are handled separately")
    @RetryableParameterizedTest(repeats = 3, source = FlakyTestArgumentProvider.class, exceptions = {AssertionError.class})
    void whenParameterizedIsFlaky_eachCaseIsHandledCorrectly(String scenario, int failures) {
        int attempt = attemptCounters.computeIfAbsent(scenario, k -> new AtomicInteger()).incrementAndGet();
        if (failures == -1 || attempt <= failures) {
            fail("Intentional failure for scenario: " + scenario);
        }
    }

    @Test
    @DisplayName("Should not retry when exception type does not match")
    @RetryableTest(repeats = 5, exceptions = {IOException.class})
    void whenExceptionTypeDoesNotMatch_itShouldNotRetry() {
        AtomicInteger counter = attemptCounters.computeIfAbsent("exception_mismatch", k -> new AtomicInteger());
        assertThrows(AssertionError.class, () -> {
            counter.incrementAndGet();
            fail("Assertion error");
        });
        assertEquals(1, counter.get());
    }
}
