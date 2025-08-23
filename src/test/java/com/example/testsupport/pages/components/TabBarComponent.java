package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

/**
 * Tab bar component for the mobile version.
 */
public class TabBarComponent extends BaseComponent {
    private final LocalizationService ls;

    public TabBarComponent(Locator root, LocalizationService ls) {
        super(root);
        this.ls = ls;
    }

    /**
     * Clicks the "Casino" tab.
     */
    public void clickCasino() {
        String casinoText = ls.get("header.menu.casino");
        root.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions()
                .setName(casinoText)
                .setExact(true))
                .click();
    }

    /**
     * Opens the user profile.
     */
    public void openProfile() {
        root.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions()
                .setName("Profile"))
                .click();
    }
}
