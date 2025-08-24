package com.example.testsupport.framework.lifecycle;

import com.example.testsupport.framework.browser.PlaywrightManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Local strategy: one browser per class, new context for each test.
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

