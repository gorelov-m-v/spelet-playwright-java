package com.example.testsupport.pages;

import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.components.HeaderComponent;
import com.example.testsupport.pages.components.TabBarComponent;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Base Page Object with shared logic.
 */
public abstract class BasePage<T extends BasePage<T>> {
    private final ObjectProvider<Page> pageProvider;
    protected final LocalizationService ls;
    private final ObjectProvider<HeaderComponent> headerProvider;
    private final ObjectProvider<TabBarComponent> tabBarProvider;
    private HeaderComponent header;
    private TabBarComponent tabBar;

    @SuppressWarnings("resource")
    protected BasePage(ObjectProvider<Page> pageProvider, LocalizationService ls,
                       ObjectProvider<HeaderComponent> headerProvider,
                       ObjectProvider<TabBarComponent> tabBarProvider) {
        this.pageProvider = pageProvider;
        this.ls = ls;
        this.headerProvider = headerProvider;
        this.tabBarProvider = tabBarProvider;
    }

    protected Page page() {
        return pageProvider.getObject();
    }

    public HeaderComponent header() {
        if (header == null) {
            header = headerProvider.getObject(page().locator("header"));
        }
        return header;
    }

    public TabBarComponent tabBar() {
        if (tabBar == null) {
            tabBar = tabBarProvider.getObject(page().locator("div.tab-bar__list"));
        }
        return tabBar;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    /**
     * Checks that the current URL contains the expected path.
     *
     * @param expectedPath expected URL substring
     */
    public void verifyUrlContains(String expectedPath) {
        String current = page().url();
        Assertions.assertTrue(
                current.contains(expectedPath),
                String.format("Expected URL to contain '%s' but was '%s'", expectedPath, current)
        );
    }

    public abstract T verifyIsLoaded();
}
