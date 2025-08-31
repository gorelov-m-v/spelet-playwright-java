package com.example.testsupport.framework.lifecycle;

import com.example.testsupport.framework.browser.PlaywrightManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Local strategy: new browser and context for each test.
 */
@Component
@Profile("local")
public class LocalPlaywrightLifecycle implements PlaywrightLifecycleStrategy {

    private final PlaywrightManager pm;

    public LocalPlaywrightLifecycle(PlaywrightManager pm) {
        this.pm = pm;
    }

    @Override
    public void beforeAll() { }

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
    public void afterAll() { }
}

