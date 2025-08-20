package com.example.testsupport.base;

import com.example.testsupport.config.AppProperties;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Factory for creating local Playwright browsers.
 */
@Component
@Profile("local")
public class LocalBrowserFactory implements BrowserFactory {

    private final AppProperties props;

    public LocalBrowserFactory(AppProperties props) {
        this.props = props;
    }

    @Override
    public Browser create(Playwright playwright) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(props.isHeadless());

        String browserName = props.getBrowser().toLowerCase();
        return switch (browserName) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> playwright.chromium().launch(options);
        };
    }
}
