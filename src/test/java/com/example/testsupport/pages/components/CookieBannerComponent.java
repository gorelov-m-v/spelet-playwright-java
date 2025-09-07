package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Component for interacting with the cookie consent banner.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CookieBannerComponent extends BaseComponent {

    private final LocalizationService ls;

    public CookieBannerComponent(Page page, LocalizationService ls) {
        super(page.locator("body"));
        this.ls = ls;
    }

    /**
     * Clicks the localized "accept cookies" button, failing if it is not found within two seconds.
     */
    public void acceptIfPresent() {
        step("Принять куки, если баннер отображается", () -> {
            String acceptText;
            try {
                acceptText = ls.get("cookies.accept");
            } catch (Exception e) {
                acceptText = "Accept";
            }
            Locator button = root().getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName(acceptText));
            try {
                button.first().waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(2000));
                button.first().click();
            } catch (PlaywrightException e) {
                Assertions.fail(String.format("Cookie accept button with text '%s' not found", acceptText));
            }
        });
    }
}
