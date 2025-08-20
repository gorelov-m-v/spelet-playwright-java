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

        BrowserName browserName = props.getBrowser();
        return switch (browserName) {
            case FIREFOX -> playwright.firefox().launch(options);
            case WEBKIT -> playwright.webkit().launch(options);
            case CHROMIUM -> playwright.chromium().launch(options);
        };
    }
}
