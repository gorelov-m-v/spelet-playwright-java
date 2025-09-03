package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Демонстрирует, что настройка test.retry позволяет перезапускать
 * flaky-тесты без аннотации @Retryable.
 */
@ExtendWith(RetryableExtension.class)
class RetryPropertyTest {

    private static int attempts = 0;
    private static final Map<Integer, Integer> paramAttempts = new HashMap<>();

    @BeforeAll
    static void enableRetries() {
        System.setProperty("test.retry", "1");
    }

    @AfterAll
    static void disableRetries() {
        System.clearProperty("test.retry");
    }

    @Test
    @DisplayName("Flaky-тест повторяется без @Retryable, если задан test.retry")
    void flakyTestRetriesWithoutAnnotation() {
        attempts++;
        if (attempts < 2) {
            Assertions.fail("Падение на попытке " + attempts);
        }
    }

    @ParameterizedTest(name = "parameter {0}")
    @ValueSource(ints = {1, 2})
    @DisplayName("Параметризованный тест также повторяется при заданном test.retry")
    void flakyParameterizedTest(int value) {
        int current = paramAttempts.merge(value, 1, Integer::sum);
        if (current < 2) {
            Assertions.fail("Падение на попытке " + current + " для значения " + value);
        }
    }
}
