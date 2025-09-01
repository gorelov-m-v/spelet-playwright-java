package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests validating retry logic.
 */
@TestMethodOrder(OrderAnnotation.class)
class RetryLogicTest {

    private static final AtomicInteger flakyCounter = new AtomicInteger();

    @RetryableTest(repeats = 2)
    @Order(1)
    void flakyTestSucceedsAfterRetry() {
        int attempt = flakyCounter.getAndIncrement();
        if (attempt < 1) {
            Assertions.fail("flaky failure");
        }
    }

    private static final AtomicInteger minSuccessCounter = new AtomicInteger();

    @RetryableTest(repeats = 2, minSuccess = 2)
    @Order(2)
    void testWithMinSuccess() {
        int attempt = minSuccessCounter.getAndIncrement();
        if (attempt == 0) {
            throw new RuntimeException("first attempt fails");
        }
        // subsequent attempts succeed
    }

    private static final ConcurrentHashMap<Integer, AtomicInteger> paramCounters = new ConcurrentHashMap<>();

    @RetryableParameterizedTest(repeats = 2)
    @ValueSource(ints = {1, 2})
    @Order(3)
    void parameterizedFlakyTest(int id) {
        AtomicInteger counter = paramCounters.computeIfAbsent(id, k -> new AtomicInteger());
        int attempt = counter.getAndIncrement();
        if (id == 1 && attempt < 1) {
            Assertions.fail("first parameter is flaky");
        }
    }
}
