package test.pages.components;

import main.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static test.framework.utils.AllureHelper.step;

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
        step("Клик по ссылке 'Казино' в хедере", () -> {
            String casinoText = ls.get("header.menu.casino");
            root.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions()
                    .setName(casinoText)
                    .setExact(true))
                .click();
        });
    }

    /**
     * Verifies that the logo is visible.
     */
    public void verifyLogoVisible() {
        step("Проверка видимости логотипа", () -> {
            assertThat(root.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions()
                .setName("Spelet"))).isVisible();
        });
    }
}
