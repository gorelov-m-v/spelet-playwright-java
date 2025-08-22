package com.example.testsupport.framework.lifecycle;

/**
 * Стратегия управления жизненным циклом Playwright для различных профилей запуска.
 */
public interface PlaywrightLifecycleStrategy {

    void beforeAll();

    void beforeEach();

    void afterEach();

    void afterAll();
}

