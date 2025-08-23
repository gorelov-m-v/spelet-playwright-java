package com.example.testsupport.framework.listeners;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.lifecycle.PlaywrightLifecycleStrategy;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JUnit 5 extension that manages the Playwright lifecycle
 * and creates helpful Allure attachments when a test fails.
 */
public class PlaywrightExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightExtension.class);
    private PlaywrightLifecycleStrategy lifecycle;

    @Override
    public void beforeAll(ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        lifecycle = ctx.getBean(PlaywrightLifecycleStrategy.class);
        lifecycle.beforeAll();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        if (lifecycle == null) {
            lifecycle = ctx.getBean(PlaywrightLifecycleStrategy.class);
        }
        LocalizationService ls = ctx.getBean(LocalizationService.class);
        AppProperties props = ctx.getBean(AppProperties.class);
        ls.loadLocale(props.getLanguage());

        lifecycle.beforeEach();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        PlaywrightManager manager = ctx.getBean(PlaywrightManager.class);

        // Check for test failure BEFORE closing the context
        if (context.getExecutionException().isPresent()) {
            log.info("Test {} failed. Capturing failure artifacts...", context.getDisplayName());
            Page page = manager.getPage();
            try {
                // 1. Attach URL and Screenshot
                Allure.addAttachment("Current URL", "text/plain", page.url());
                byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                Allure.getLifecycle().addAttachment("Screenshot", "image/png", "png", screenshot);

                // 2. Save and attach Playwright Trace
                Path tracePath = manager.saveTrace(context.getDisplayName());
                if (tracePath != null && Files.exists(tracePath)) {
                    try (InputStream traceStream = Files.newInputStream(tracePath)) {
                        Allure.addAttachment("Playwright Trace", "application/zip", traceStream, "zip");
                    }
                } else {
                    log.warn("Trace file was not created for failed test: {}", context.getDisplayName());
                }

            } catch (Throwable e) {
                log.error("Critical error while creating Allure attachments for failed test.", e);
            }
        }

        // Finally, always run the lifecycle cleanup
        lifecycle.afterEach();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (lifecycle != null) {
            lifecycle.afterAll();
        }
    }
}

