package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

/**
 * Компонент хедера для десктопной версии.
 */
public class HeaderComponent extends BaseComponent {
    private final LocalizationService ls;

    public HeaderComponent(Locator root, LocalizationService ls) {
        super(root);
        this.ls = ls;
    }

    /**
     * Кликает по ссылке «Казино» в хедере.
     */
    public void clickCasino() {
        String casinoText = ls.get("header.menu.casino");
        root.getByRole(AriaRole.NAVIGATION)
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions()
                        .setName(casinoText)
                        .setExact(true))
                .click();
    }

    /**
     * Возвращает локатор логотипа.
     */
    public Locator getLogo() {
        return root.locator("a.logo");
    }
}
