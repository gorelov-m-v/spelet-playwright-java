package com.example.testsupport.framework.lifecycle;

import com.example.testsupport.framework.browser.PlaywrightManager;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Стратегия жизненного цикла для запуска в BrowserStack.
 * Для каждого теста создаётся отдельная сессия BrowserStack.
 */
@Component
@Profile("browserstack")
public class BrowserStackPlaywrightLifecycle implements PlaywrightLifecycleStrategy {

    private final PlaywrightManager manager;

    public BrowserStackPlaywrightLifecycle(PlaywrightManager manager) {
        this.manager = manager;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        System.setProperty("bs.name", context.getDisplayName());
        manager.initializeBrowser();
        manager.createContextAndPage();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        String status = context.getExecutionException().isPresent() ? "failed" : "passed";
        String reason = context.getExecutionException().map(Throwable::getMessage).orElse("");
        try {
            manager.getPage().evaluate(
                    "({status,reason}) => window.browserstack_executor({action: 'setSessionStatus', arguments: {status, reason}})",
                    Map.of("status", status, "reason", reason));
        } catch (RuntimeException ignored) {
            // Игнорируем сбои при установке статуса, чтобы не скрыть основную ошибку теста
        }
        manager.closeAll();
    }
}

