package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Execution(ExecutionMode.CONCURRENT)
class RetryLogicTest {

    private static final Map<String, AtomicInteger> attemptCounters = new ConcurrentHashMap<>();

    @BeforeAll
    static void setup() {
        attemptCounters.clear();
    }

    private static AtomicInteger counter(String key) {
        return attemptCounters.computeIfAbsent(key, k -> new AtomicInteger());
    }

    // Scenario 1
    @RetryableTest(repeats = 1)
    void whenTestIsFlaky_itShouldSucceedOnRetry() {
        AtomicInteger c = counter("flaky");
        if (c.incrementAndGet() < 2) {
            Assertions.fail("Flaky failure");
        }
        Assertions.assertEquals(2, c.get());
    }

    // Scenario 2
    @RetryableTest(repeats = 2)
    void whenTestAlwaysFails_itShouldThrowAfterAllAttempts() {
        AtomicInteger c = counter("always");
        Assertions.assertThrows(AssertionError.class, () -> {
            if (c.incrementAndGet() <= 3) {
                Assertions.fail("Always fails");
            }
        });
        Assertions.assertEquals(3, c.get());
    }

    // Scenario 3
    @RetryableParameterizedTest(repeats = 3, source = FlakyTestArgumentProvider.class)
    void whenParameterizedIsFlaky_itShouldSucceedOnRetry(String scenario, int failures) {
        AtomicInteger c = counter(scenario);
        if (failures == -1) {
            Assertions.assertThrows(AssertionError.class, () -> {
                for (int i = 0; i < 4; i++) {
                    c.incrementAndGet();
                    Assertions.fail("Always failing");
                }
            });
            Assertions.assertEquals(4, c.get());
        } else {
            if (c.getAndIncrement() < failures) {
                Assertions.fail("flaky param");
            }
            Assertions.assertEquals(failures + 1, c.get());
        }
    }

    // Scenario 4
    @RetryableTest(repeats = 5, exceptions = {IOException.class})
    void whenExceptionTypeDoesNotMatch_itShouldFailWithoutRetry() {
        AtomicInteger c = counter("exception");
        Assertions.assertThrows(AssertionError.class, () -> {
            c.incrementAndGet();
            Assertions.fail("Wrong exception");
        });
        Assertions.assertEquals(1, c.get());
    }
}
