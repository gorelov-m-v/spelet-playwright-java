package test.pages.components;

import main.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static test.framework.utils.AllureHelper.step;

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
            assertThat(root.getByText(title, new Locator.GetByTextOptions()
                    .setExact(true))).isVisible();
            return this;
        });
    }
}
