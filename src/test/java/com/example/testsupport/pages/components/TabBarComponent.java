package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

/**
 * Компонент таббара для мобильной версии.
 */
public class TabBarComponent extends BaseComponent {
    private final LocalizationService ls;

    public TabBarComponent(Locator root, LocalizationService ls) {
        super(root);
        this.ls = ls;
    }

    /**
     * Кликает по вкладке «Казино» в таббаре.
     */
    public void clickCasino() {
        String casinoText = ls.get("header.menu.casino");
        root.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions()
                .setName(casinoText)
                .setExact(true))
                .click();
    }

    /**
     * Открывает профиль пользователя.
     */
    public void openProfile() {
        root.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions()
                .setName("Profile"))
                .click();
    }
}
