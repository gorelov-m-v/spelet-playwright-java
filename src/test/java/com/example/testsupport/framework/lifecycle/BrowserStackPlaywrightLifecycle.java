package com.example.testsupport.framework.lifecycle;

import com.example.testsupport.framework.browser.PlaywrightManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Стратегия для BrowserStack: новый браузер и контекст на каждый тест.
 */
@Component
@Profile("browserstack")
public class BrowserStackPlaywrightLifecycle implements PlaywrightLifecycleStrategy {

    private final PlaywrightManager pm;

    public BrowserStackPlaywrightLifecycle(PlaywrightManager pm) {
        this.pm = pm;
    }

    @Override
    public void beforeAll() {
        // no-op
    }

    @Override
    public void beforeEach() {
        pm.initializeBrowser();
        pm.createContextAndPage();
    }

    @Override
    public void afterEach() {
        pm.closeContextAndPage();
        pm.closeBrowser();
    }

    @Override
    public void afterAll() {
        // no-op
    }
}

