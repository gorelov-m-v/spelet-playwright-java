package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Component representing the authorization prompt modal that appears when
 * attempting to launch a game without being logged in.
 */
public class AuthModalComponent extends BaseComponent {

    private final LocalizationService ls;

    public AuthModalComponent(Locator root, LocalizationService ls) {
        super(root);
        this.ls = ls;
    }

    /**
     * Verifies that the modal is visible by asserting its translated title.
     *
     * @return current component instance
     */
    public AuthModalComponent verifyIsLoaded() {
        return step("Проверяем отображение модального окна авторизации", () -> {
            String title = ls.get("casino.play.prompt");
            assertThat(root.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions()
                    .setName(title)
                    .setExact(true))).isVisible();
            return this;
        });
    }
}
