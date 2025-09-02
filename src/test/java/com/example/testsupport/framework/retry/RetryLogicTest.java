package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@DisplayName("Проверка механизма ретраев на InvocationInterceptor")
@ExtendWith(RetryableExtension.class)
public class RetryLogicTest {

    private static final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @Test
    @DisplayName("Обычный flaky-тест")
    @Retryable(repeats = 2)
    void flakyTest() {
        int attempt = counters.computeIfAbsent("flaky", k -> new AtomicInteger()).incrementAndGet();
        if (attempt < 2) {
            Assertions.fail("Падение на попытке " + attempt);
        }
    }

    @DisplayName("Параметризованный flaky-тест")
    @ParameterizedTest
    @ValueSource(strings = {"A", "B"})
    @Retryable(repeats = 2)
    void parameterizedFlakyTest(String scenario) {
        int attempt = counters.computeIfAbsent(scenario, k -> new AtomicInteger()).incrementAndGet();
        if ("A".equals(scenario) && attempt < 2) {
            Assertions.fail("Сценарий A падает на попытке " + attempt);
        }
        System.out.printf("Сценарий %s, попытка %d - успех%n", scenario, attempt);
    }
}
