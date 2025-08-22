package com.example.testsupport.pages;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Базовый Page Object с общей логикой.
 */
public abstract class BasePage {
    private final ObjectProvider<Page> pageProvider;
    protected final LocalizationService ls;

    protected BasePage(ObjectProvider<Page> pageProvider, LocalizationService ls) {
        this.pageProvider = pageProvider;
        this.ls = ls;
    }

    protected Page page() {
        return pageProvider.getObject();
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
