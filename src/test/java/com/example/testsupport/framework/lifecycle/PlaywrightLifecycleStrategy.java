package com.example.testsupport.framework.lifecycle;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Стратегия управления жизненным циклом Playwright.
 * В зависимости от активного профиля Spring будет выбрана необходимая реализация.
 */
public interface PlaywrightLifecycleStrategy {

    /** Выполняется один раз перед всеми тестами в классе. */
    default void beforeAll(ExtensionContext context) {}

    /** Выполняется перед каждым тестовым методом. */
    default void beforeEach(ExtensionContext context) {}

    /** Выполняется после каждого тестового метода. */
    default void afterEach(ExtensionContext context) {}

    /** Выполняется один раз после всех тестов в классе. */
    default void afterAll(ExtensionContext context) {}
}

