package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Execution(ExecutionMode.CONCURRENT)
class RetryLogicTest {

    private static final Map<String, AtomicInteger> attemptCounters = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        attemptCounters.clear();
    }

    @RetryableTest(repeats = 1)
    @Test
    void whenTestIsFlaky_itShouldSucceedOnRetry() {
        AtomicInteger counter = attemptCounters.computeIfAbsent("flaky", k -> new AtomicInteger());
        int attempt = counter.incrementAndGet();
        if (attempt < 2) {
            Assertions.fail("Flaky failure");
        }
        Assertions.assertEquals(2, counter.get());
    }

    @RetryableTest(repeats = 2)
    @Test
    void whenTestAlwaysFails_itShouldThrowAfterAllAttempts() {
        AtomicInteger counter = attemptCounters.computeIfAbsent("alwaysFail", k -> new AtomicInteger());
        Assertions.assertThrows(AssertionError.class, () -> {
            counter.incrementAndGet();
            Assertions.fail("Always fail");
        });
        Assertions.assertEquals(3, counter.get());
    }

    @RetryableParameterizedTest(repeats = 3, source = FlakyTestArgumentProvider.class)
    void whenParameterizedIsFlaky_itShouldSucceedOnRetry(String scenario, int failures) {
        AtomicInteger counter = attemptCounters.computeIfAbsent(scenario, k -> new AtomicInteger());
        if (failures == -1) {
            Assertions.assertThrows(AssertionError.class, () -> {
                counter.incrementAndGet();
                Assertions.fail("Always fails");
            });
            Assertions.assertEquals(4, counter.get());
            return;
        }
        int attempt = counter.incrementAndGet();
        if (attempt <= failures) {
            Assertions.fail("Flaky failure");
        }
        Assertions.assertEquals(failures + 1, counter.get());
    }

    @RetryableTest(repeats = 5, exceptions = {java.io.IOException.class})
    @Test
    void whenExceptionTypeDoesNotMatch_itShouldFailWithoutRetry() {
        AtomicInteger counter = attemptCounters.computeIfAbsent("exceptionFilter", k -> new AtomicInteger());
        Assertions.assertThrows(AssertionError.class, () -> {
            counter.incrementAndGet();
            Assertions.fail("Non-IO failure");
        });
        Assertions.assertEquals(1, counter.get());
    }
}
