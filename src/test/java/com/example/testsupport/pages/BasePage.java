package com.example.testsupport.pages;

import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.components.HeaderComponent;
import com.example.testsupport.pages.components.TabBarComponent;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Базовый Page Object с общей логикой.
 */
public abstract class BasePage {
    private final ObjectProvider<Page> pageProvider;
    protected final LocalizationService ls;
    private final HeaderComponent header;
    private final TabBarComponent tabBar;

    protected BasePage(ObjectProvider<Page> pageProvider, LocalizationService ls) {
        this.pageProvider = pageProvider;
        this.ls = ls;
        this.header = new HeaderComponent(page().locator("header[role='banner']"), ls);
        this.tabBar = new TabBarComponent(page().locator("div.tab-bar__list"), ls);
    }

    protected Page page() {
        return pageProvider.getObject();
    }

    public HeaderComponent header() {
        return header;
    }

    public TabBarComponent tabBar() {
        return tabBar;
    }

    /**
     * Проверяет, что текущий URL содержит ожидаемый путь.
     *
     * @param expectedPath ожидаемая подстрока URL
     */
    public void verifyUrlContains(String expectedPath) {
        String current = page().url();
        Assertions.assertTrue(
                current.contains(expectedPath),
                String.format("Ожидалось, что URL содержит '%s', но фактический URL '%s'", expectedPath, current)
        );
    }
}
