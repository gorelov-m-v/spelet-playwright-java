package com.example.testsupport.framework.lifecycle;

/**
 * Playwright lifecycle strategy for different run profiles.
 */
public interface PlaywrightLifecycleStrategy {

    void beforeAll();

    void beforeEach();

    void afterEach();

    void afterAll();
}

