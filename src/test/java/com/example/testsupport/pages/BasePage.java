package com.example.testsupport.pages;

import com.example.testsupport.config.EnvironmentConfig;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.components.HeaderComponent;
import com.example.testsupport.pages.components.TabBarComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Base Page Object with shared logic.
 */
public abstract class BasePage<T extends BasePage<T>> {
    protected final Page page;
    protected final LocalizationService ls;
    protected final EnvironmentConfig config;
    private final HeaderComponent header;
    private final TabBarComponent tabBar;

    @SuppressWarnings("resource")
    protected BasePage(Page page,
                       LocalizationService ls,
                       EnvironmentConfig config,
                       ObjectProvider<HeaderComponent> headerProvider,
                       ObjectProvider<TabBarComponent> tabBarProvider) {
        this.page = page;
        this.ls = ls;
        this.config = config;
        this.header = headerProvider.getObject();
        this.tabBar = tabBarProvider.getObject();
    }

    public HeaderComponent header() {
        return header;
    }

    public TabBarComponent tabBar() {
        return tabBar;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    private String buildBaseUrlForCurrentLanguage() {
        String lang = ls.getCurrentLangCode();
        String base = config.getApi().getBaseUrl();
        if (lang == null || lang.equals(config.getBrowser().getDefaultLanguage())) {
            return base;
        }
        return base + "/" + lang;
    }

    public T open() {
        page.navigate(buildBaseUrlForCurrentLanguage());
        return self();
    }

    public T navigate(String path) {
        page.navigate(buildBaseUrlForCurrentLanguage() + path);
        return self();
    }

    /**
     * Checks that the current URL contains the expected path.
     *
     * @param expectedPath expected URL substring
     */
    public void verifyUrlContains(String expectedPath) {
        String current = page.url();
        Assertions.assertTrue(
                current.contains(expectedPath),
                String.format("Expected URL to contain '%s' but was '%s'", expectedPath, current)
        );
    }

    public abstract T verifyIsLoaded();

    protected void acceptCookiesIfPresent() {
        String acceptText;
        try {
            acceptText = ls.get("cookies.accept");
        } catch (Exception e) {
            acceptText = "Accept";
        }
        Locator button = page.locator(String.format("button:has-text('%s')", acceptText));
        if (button.first().isVisible()) {
            button.first().click();
        }
    }
}
