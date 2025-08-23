package com.example.testsupport.framework.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

/**
 * Factory interface for creating Playwright {@link Browser} instances.
 */
public interface BrowserFactory {
    /**
     * Creates a browser using the provided Playwright engine.
     *
     * @param playwright initialized Playwright instance
     * @return created browser
     */
    Browser create(Playwright playwright);
}
