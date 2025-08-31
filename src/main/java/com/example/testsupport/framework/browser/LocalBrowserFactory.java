package com.example.testsupport.framework.browser;

import com.example.testsupport.config.EnvironmentConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Factory for launching local Playwright browsers.
 */
@Component
@Profile("spelet")
public class LocalBrowserFactory implements BrowserFactory {

    private final EnvironmentConfig config;

    public LocalBrowserFactory(EnvironmentConfig config) {
        this.config = config;
    }

    @Override
    public Browser create(Playwright playwright) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(config.getBrowser().isHeadless());

        BrowserName browserName = config.getBrowser().getName();
        return switch (browserName) {
            case FIREFOX -> playwright.firefox().launch(options);
            case WEBKIT -> playwright.webkit().launch(options);
            case CHROMIUM -> playwright.chromium().launch(options);
        };
    }
}
