package com.example.testsupport.pages;

import com.example.testsupport.config.AppProperties;
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
    private final ObjectProvider<Page> pageProvider;
    protected final LocalizationService ls;
    protected final AppProperties props;
    private final HeaderComponent header;
    private final TabBarComponent tabBar;
    private final Locator headerRoot;
    private final Locator tabBarRoot;

    @SuppressWarnings("resource")
    protected BasePage(ObjectProvider<Page> pageProvider, LocalizationService ls, AppProperties props) {
        this.pageProvider = pageProvider;
        this.ls = ls;
        this.props = props;
        this.headerRoot = page().locator("header");
        this.tabBarRoot = page().locator("div.tab-bar__list");
        this.header = new HeaderComponent(headerRoot, ls);
        this.tabBar = new TabBarComponent(tabBarRoot, ls);
    }

    protected Page page() {
        return pageProvider.getObject();
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
        if (lang == null || lang.equals(props.getDefaultLanguage())) {
            return props.getBaseUrl();
        }
        return props.getBaseUrl() + "/" + lang;
    }

    public T open() {
        page().navigate(buildBaseUrlForCurrentLanguage());
        return self();
    }

    public T navigate(String path) {
        page().navigate(buildBaseUrlForCurrentLanguage() + path);
        return self();
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

