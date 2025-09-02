package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@DisplayName("Проверка механизма ретраев")
@Execution(ExecutionMode.CONCURRENT)
public class RetryLogicTest {

    private static final Map<String, AtomicInteger> attemptCounters = new ConcurrentHashMap<>();

    @BeforeEach
    void resetCounters() { attemptCounters.clear(); }

    @DisplayName("Обычный flaky-тест должен пройти после ретрая")
    @RetryableTest(repeats = 2)
    void whenTestIsFlaky_itShouldSucceed() {
        int attempt = attemptCounters.computeIfAbsent("flaky_simple", k -> new AtomicInteger()).incrementAndGet();
        if (attempt < 2) {
            Assertions.fail("Падение на попытке " + attempt);
        }
    }

    @DisplayName("Параметризованный flaky-тест")
    @RetryableParameterizedTest(repeats = 3)
    @ArgumentsSource(FlakyTestArgumentProvider.class)
    void whenParameterizedIsFlaky_eachCaseIsHandledCorrectly(String scenario, int failures) {
        int attempt = attemptCounters.computeIfAbsent(scenario, k -> new AtomicInteger()).incrementAndGet();
        if (failures == -1 || attempt <= failures) {
            Assertions.fail(String.format("Сценарий '%s' падает на попытке #%d", scenario, attempt));
        }
    }
}
