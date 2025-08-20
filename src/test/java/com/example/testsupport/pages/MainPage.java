package com.example.testsupport.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.example.testsupport.framework.localization.LocalizationService;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MainPage {
    private final Page page;
    private final LocalizationService loc;

    public MainPage(Page page, LocalizationService loc) {
        this.page = page;
        this.loc = loc;
    }

    /**
     * Возвращает локатор ссылки «Казино» в меню.
     *
     * @return локатор ссылки
     */
    public Locator casinoLink() {
        String casinoText = loc.get("header.menu.casino");
        return page.getByRole(AriaRole.NAVIGATION)
                .getByRole(AriaRole.LINK,
                        new Locator.GetByRoleOptions()
                                .setName(casinoText)
                                .setExact(true));
    }

    /**
     * Переходит на страницу казино через меню.
     */
    public void clickCasino() {
        page.waitForNavigation(() -> casinoLink().click());
    }

    /**
     * Проверяет, что текущий URL содержит ожидаемый путь.
     *
     * @param expectedPath ожидаемая подстрока URL
     * @return текущий объект {@link MainPage}
     */
    public MainPage verifyUrlContains(String expectedPath) {
        String current = page.url();
        Assertions.assertTrue(
                current.contains(expectedPath),
                String.format("Ожидалось, что URL содержит '%s', но фактический URL '%s'", expectedPath, current)
        );
        return this;
    }
}
