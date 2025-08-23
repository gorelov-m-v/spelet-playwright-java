package com.example.testsupport.pages;

import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.components.HeaderComponent;
import com.example.testsupport.pages.components.TabBarComponent;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.ObjectProvider;
import java.util.function.Supplier;

/**
 * Base Page Object with shared logic.
 */
public abstract class BasePage {
    private final ObjectProvider<Page> pageProvider;
    protected final LocalizationService ls;
    private final Supplier<HeaderComponent> header;
    private final Supplier<TabBarComponent> tabBar;

    @SuppressWarnings("resource")
    protected BasePage(ObjectProvider<Page> pageProvider, LocalizationService ls) {
        this.pageProvider = pageProvider;
        this.ls = ls;
        this.header = memoize(() -> new HeaderComponent(page().getByRole(AriaRole.NAVIGATION), ls));
        this.tabBar = memoize(() -> new TabBarComponent(page().locator("div.tab-bar__list"), ls));
    }

    protected Page page() {
        return pageProvider.getObject();
    }

    public HeaderComponent header() {
        return header.get();
    }

    public TabBarComponent tabBar() {
        return tabBar.get();
    }

    private static <T> Supplier<T> memoize(Supplier<T> supplier) {
        return new Supplier<>() {
            private T value;
            @Override
            public T get() {
                if (value == null) {
                    value = supplier.get();
                }
                return value;
            }
        };
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
}
