package com.example.testsupport.base;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * JUnit 5 extension that manages Playwright lifecycle and creates useful
 * Allure attachments when a test fails.
 */
public class PlaywrightExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        PlaywrightManager manager = SpringExtension.getApplicationContext(context)
                .getBean(PlaywrightManager.class);
        // Initialize Playwright resources for the current test
        manager.getPage();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        PlaywrightManager manager = SpringExtension.getApplicationContext(context)
                .getBean(PlaywrightManager.class);
        try {
            if (context.getExecutionException().isPresent()) {
                Page page = manager.getPage();
                try {
                    Allure.addAttachment("Current URL", "text/plain", page.url());
                    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                    Allure.getLifecycle().addAttachment("Screenshot", "image/png", "png", screenshot);
                } catch (Throwable e) {
                    log.error("Failed to create allure attachments", e);
                }
            }
        } finally {
            manager.close();
        }
    }
}

