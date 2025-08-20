package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assertions;

public class MainPage {
    private final Page page;

    public MainPage(Page page) {
        this.page = page;
    }

    public void clickKazino() {
        Locator kazino = page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Kazino")
        );
        if (!kazino.first().isVisible()) {
            kazino = page.locator("a[href='/casino']");
        }
        kazino.first().click();
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
