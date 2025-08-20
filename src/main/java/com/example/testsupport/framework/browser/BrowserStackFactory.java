package com.example.testsupport.framework.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Фабрика, создающая браузеры, подключенные к BrowserStack.
 */
@Component
@Profile("browserstack")
public class BrowserStackFactory implements BrowserFactory {

    private final BrowserStackClient bsClient;

    public BrowserStackFactory(BrowserStackClient bsClient) {
        this.bsClient = bsClient;
    }

    @Override
    public Browser create(Playwright playwright) {
        return bsClient.connectBrowser(playwright);
    }
}
