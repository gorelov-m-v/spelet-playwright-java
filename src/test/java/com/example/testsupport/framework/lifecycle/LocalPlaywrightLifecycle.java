package com.example.testsupport.framework.lifecycle;

import com.example.testsupport.framework.browser.PlaywrightManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Локальная стратегия: один браузер на класс, новый контекст на каждый тест.
 */
@Component
@Profile("local")
public class LocalPlaywrightLifecycle implements PlaywrightLifecycleStrategy {

    private final PlaywrightManager pm;

    public LocalPlaywrightLifecycle(PlaywrightManager pm) {
        this.pm = pm;
    }

    @Override
    public void beforeAll() {
        pm.initializeBrowser();
    }

    @Override
    public void beforeEach() {
        pm.createContextAndPage();
    }

    @Override
    public void afterEach() {
        pm.closeContextAndPage();
    }

    @Override
    public void afterAll() {
        pm.closeBrowser();
    }
}

