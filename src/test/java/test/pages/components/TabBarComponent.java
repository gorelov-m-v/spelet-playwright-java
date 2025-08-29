package test.pages.components;

import main.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;
import static test.framework.utils.AllureHelper.step;

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
        step("Клик по табу 'Казино'", () -> {
            String casinoText = ls.get("header.menu.casino");
            root.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions()
                    .setName(casinoText)
                    .setExact(true))
                    .click();
        });
    }

    /**
     * Opens the user profile.
     */
    public void openProfile() {
        step("Открытие профиля пользователя", () ->
            root.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions()
                    .setName("Profile"))
                    .click()
        );
    }
}
