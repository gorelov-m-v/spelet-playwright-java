package com.example.testsupport.framework.retry;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
class RetryLogicTest {

    private static final Map<String, AtomicInteger> attemptCounters = new ConcurrentHashMap<>();

    @BeforeEach
    void reset() {
        attemptCounters.clear();
    }

    @Test
    @RetryableTest(repeats = 1)
    void whenTestIsFlaky_itShouldSucceedOnRetry() {
        AtomicInteger counter = attemptCounters.computeIfAbsent("flaky", k -> new AtomicInteger());
        int attempt = counter.incrementAndGet();
        if (attempt < 2) {
            Assertions.fail("Flaky failure");
        }
        Assertions.assertEquals(2, counter.get());
    }

    @Test
    @RetryableTest(repeats = 2)
    void whenTestAlwaysFails_itShouldThrowAfterAllAttempts() {
        AtomicInteger counter = attemptCounters.computeIfAbsent("alwaysFail", k -> new AtomicInteger());
        Assertions.assertThrows(AssertionError.class, () -> {
            counter.incrementAndGet();
            Assertions.fail("Always failing");
        });
        Assertions.assertEquals(3, counter.get());
    }

    @RetryableParameterizedTest(repeats = 3, source = FlakyTestArgumentProvider.class)
    void whenParameterizedIsFlaky_itShouldSucceedOnRetry(String scenario, int failures) {
        AtomicInteger counter = attemptCounters.computeIfAbsent(scenario, k -> new AtomicInteger());
        if (failures == -1) {
            Assertions.assertThrows(AssertionError.class, () -> {
                counter.incrementAndGet();
                Assertions.fail("Always failing");
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

    @Test
    @RetryableTest(repeats = 5, exceptions = {IOException.class})
    void whenExceptionTypeDoesNotMatch_itShouldFailWithoutRetry() {
        AtomicInteger counter = attemptCounters.computeIfAbsent("exceptionType", k -> new AtomicInteger());
        Assertions.assertThrows(AssertionError.class, () -> {
            counter.incrementAndGet();
            Assertions.fail("Failure");
        });
        Assertions.assertEquals(1, counter.get());
    }
}
