package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Демонстрирует, что настройка test.retry позволяет перезапускать
 * flaky-тесты без аннотации @Retryable.
 */
@ExtendWith(RetryableExtension.class)
class RetryPropertyTest {

    private static int attempts = 0;

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
}
