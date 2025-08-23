package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Header component for the desktop version.
 */
public class HeaderComponent extends BaseComponent {
    private final LocalizationService ls;

    public HeaderComponent(Locator root, LocalizationService ls) {
        super(root);
        this.ls = ls;
    }

    /**
     * Clicks the "Casino" link in the header.
     */
    public void clickCasino() {
        String casinoText = ls.get("header.menu.casino");
        root.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions()
                .setName(casinoText)
                .setExact(true))
            .click();
    }

    /**
     * Verifies that the logo is visible.
     */
    public void verifyLogoVisible() {
        assertThat(root.locator("a.logo")).isVisible();
    }
}
