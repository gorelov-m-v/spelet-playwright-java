package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Header component for the desktop version.
 */
public class HeaderComponent extends BaseComponent {
    private final Locator casinoLink;
    private final Locator logoLink;
    private final LocalizationService ls;

    public HeaderComponent(Locator root, LocalizationService ls) {
        super(root);
        this.ls = ls;
        String casinoText = ls.get("header.menu.casino");
        this.casinoLink = root.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(casinoText).setExact(true));
        this.logoLink = root.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Spelet"));
    }

    /**
     * Clicks the "Casino" link in the header.
     */
    public void clickCasino() {
        step("Клик по ссылке 'Казино' в хедере", () -> casinoLink.click());
    }

    /**
     * Verifies that the logo is visible.
     */
    public void verifyLogoVisible() {
        step("Проверка видимости логотипа", () -> {
            assertThat(logoLink).isVisible();
        });
    }
}

