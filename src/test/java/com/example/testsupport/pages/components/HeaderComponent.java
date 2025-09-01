package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Header component for the desktop version.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HeaderComponent extends BaseComponent {
    private final Locator casinoLink;
    private final Locator logoLink;

    public HeaderComponent(Page page, LocalizationService ls) {
        super(page.locator("header"));
        String casinoText = ls.get("header.menu.casino");
        this.casinoLink = root().getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions()
                        .setName(Pattern.compile("^" + Pattern.quote(casinoText), Pattern.CASE_INSENSITIVE)));
        this.logoLink = root().getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Spelet"));
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

