package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import io.qameta.allure.Epic;
import com.example.testsupport.framework.allure.Suite;
import com.example.testsupport.framework.allure.CustomSuiteExtension;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Epic("Unit-tests")
@Suite("Unit-tests: Retry")
@Tag("Unit-test")
@DisplayName("Проверка механизма ретраев (InvocationInterceptor)")
@ExtendWith({RetryableExtension.class, CustomSuiteExtension.class})
public class RetryLogicTest {

    private static final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    @BeforeEach
    void resetCounters() {
        counters.clear();
    }

    @Test
    @DisplayName("Flaky-тест должен пройти после 1 перезапуска")
    @Retryable(repeats = 1)
    void whenTestIsFlaky_itShouldSucceed() {
        int attempt = counters.computeIfAbsent("flaky", k -> new AtomicInteger(0)).incrementAndGet();
        System.out.printf("Flaky-тест, попытка #%d%n", attempt);
        if (attempt < 2) {
            Assertions.fail("Падение на попытке " + attempt);
        }
    }

    @Test
    @DisplayName("Тест, который всегда падает, должен остаться FAILED")
    @Retryable(repeats = 2)
    void whenTestAlwaysFails_itShouldRemainFailed() {
        Assertions.assertThrows(AssertionError.class, () -> {
            int attempt = counters.computeIfAbsent("alwaysFails", k -> new AtomicInteger(0)).incrementAndGet();
            System.out.printf("Always-fails тест, попытка #%d%n", attempt);
            Assertions.fail("Постоянное падение на попытке " + attempt);
        });
        Assertions.assertEquals(3, counters.get("alwaysFails").get());
    }

    @DisplayName("Параметризованный flaky-тест")
    @ParameterizedTest
    @ValueSource(strings = {"A", "B"})
    @Retryable(repeats = 2)
    void whenParameterizedIsFlaky_invocationsAreIsolated(String scenario) {
        int attempt = counters.computeIfAbsent(scenario, k -> new AtomicInteger(0)).incrementAndGet();
        System.out.printf("Параметризованный тест (сценарий %s), попытка #%d%n", scenario, attempt);
        if (scenario.equals("A") && attempt < 2) {
            Assertions.fail("Сценарий A падает на попытке " + attempt);
        }
    }

    @Test
    @DisplayName("Фильтр по исключениям не должен перезапускать тест с другой ошибкой")
    @Retryable(repeats = 5, onExceptions = {IOException.class})
    void whenExceptionTypeDoesNotMatch_itShouldNotRetry() {
         Assertions.assertThrows(AssertionError.class, () -> {
            counters.computeIfAbsent("wrongException", k -> new AtomicInteger(0)).incrementAndGet();
            Assertions.fail("Эта ошибка не IOException, ретрая быть не должно");
        });
        Assertions.assertEquals(1, counters.get("wrongException").get());
    }
}
