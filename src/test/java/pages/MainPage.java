package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.example.testsupport.localization.LocalizationService;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MainPage {
    private final Page page;
    private final LocalizationService loc;

    public MainPage(Page page, LocalizationService loc) {
        this.page = page;
        this.loc = loc;
    }

    public Locator kazinoLink() {
        String kazinoText = loc.get("header.menu.casino");
        return page.locator("nav >> text='" + kazinoText + "'");
    }

    public void clickKazino() {
        kazinoLink().first().click();
        page.waitForURL("**/casino*");
    }

    public MainPage verifyUrlContains(String expectedPath) {
        String current = page.url();
        Assertions.assertTrue(
                current.contains(expectedPath),
                String.format("Ожидалось, что URL содержит '%s', но фактический URL '%s'", expectedPath, current)
        );
        return this;
    }
}
