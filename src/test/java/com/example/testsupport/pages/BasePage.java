package com.example.testsupport.pages;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;

/**
 * Базовый Page Object с общей логикой.
 */
public abstract class BasePage {
    protected final Page page;
    protected final LocalizationService ls;

    protected BasePage(Page page, LocalizationService ls) {
        this.page = page;
        this.ls = ls;
    }

    /**
     * Проверяет, что текущий URL содержит ожидаемый путь.
     *
     * @param expectedPath ожидаемая подстрока URL
     */
    public void verifyUrlContains(String expectedPath) {
        String current = page.url();
        Assertions.assertTrue(
                current.contains(expectedPath),
                String.format("Ожидалось, что URL содержит '%s', но фактический URL '%s'", expectedPath, current)
        );
    }
}
