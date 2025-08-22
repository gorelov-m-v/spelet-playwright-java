package com.example.testsupport.framework.lifecycle;

import com.example.testsupport.framework.browser.PlaywrightManager;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Стратегия жизненного цикла для локального запуска.
 * Инициализирует браузер один раз на класс и создаёт новый контекст для каждого теста.
 */
@Component
@Profile("local")
public class LocalPlaywrightLifecycle implements PlaywrightLifecycleStrategy {

    private final PlaywrightManager manager;

    public LocalPlaywrightLifecycle(PlaywrightManager manager) {
        this.manager = manager;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        manager.initializeBrowser();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        manager.createContextAndPage();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        manager.closeContext();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        manager.closeBrowser();
    }
}

