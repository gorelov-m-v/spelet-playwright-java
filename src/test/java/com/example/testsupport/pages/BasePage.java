package com.example.testsupport.pages;

import com.example.testsupport.config.AppProperties;
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
    protected final Page page;
    protected final LocalizationService ls;
    protected final AppProperties props;
    private final HeaderComponent header;
    private final TabBarComponent tabBar;

    @SuppressWarnings("resource")
    protected BasePage(Page page,
                       LocalizationService ls,
                       AppProperties props,
                       ObjectProvider<HeaderComponent> headerProvider,
                       ObjectProvider<TabBarComponent> tabBarProvider) {
        this.page = page;
        this.ls = ls;
        this.props = props;
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
        if (lang == null || lang.equals(props.getDefaultLanguage())) {
            return props.getBaseUrl();
        }
        return props.getBaseUrl() + "/" + lang;
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
}

