package com.example.testsupport.framework.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

/**
 * Фабричный интерфейс для создания экземпляров {@link Browser} Playwright.
 */
public interface BrowserFactory {
    /**
     * Создает браузер, используя переданный движок Playwright.
     *
     * @param playwright инициализированный экземпляр Playwright
     * @return созданный браузер
     */
    Browser create(Playwright playwright);
}
