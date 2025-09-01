package com.example.testsupport.framework.listeners;

import com.example.testsupport.config.EnvironmentConfig;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.lifecycle.PlaywrightLifecycleStrategy;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import io.qameta.allure.Allure;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        EnvironmentConfig config = ctx.getBean(EnvironmentConfig.class);
        ls.loadLocale(config.getBrowser().getLanguage());

        PlaywrightManager manager = ctx.getBean(PlaywrightManager.class);
        manager.clearConsoleMessages();

        lifecycle.beforeEach();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        PlaywrightManager manager = ctx.getBean(PlaywrightManager.class);

        // Check for test failure BEFORE closing the context
        if (context.getExecutionException().isPresent()) {
            String attempt = System.getProperty("org.gradle.test-retry.currentAttempt", "1");
            String max = System.getProperty("org.gradle.test-retry.maxRetries", "0");
            String suffix = String.format("_attempt-%s-of-%s", attempt, max.equals("0") ? "0" : String.valueOf(Integer.parseInt(max) + 1));

            log.info("Test {} failed. Capturing failure artifacts...", context.getDisplayName());
            Page page = manager.getPage();
            if (page != null) {
                try {
                    Allure.addAttachment("Current URL" + suffix, "text/plain", page.url());
                } catch (PlaywrightException e) {
                    log.warn("Unable to capture URL for failed test: {}", e.getMessage());
                }
                try {
                    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                    Allure.getLifecycle().addAttachment("Screenshot" + suffix, "image/png", "png", screenshot);
                } catch (PlaywrightException e) {
                    log.warn("Unable to capture screenshot for failed test: {}", e.getMessage());
                }
            } else {
                log.warn("Playwright page is not available to capture failure artifacts.");
            }

            try {
                String consoleLog = String.join(System.lineSeparator(), manager.getConsoleMessages());
                if (!consoleLog.isEmpty()) {
                    Allure.addAttachment("Browser Console Logs" + suffix, "text/plain", consoleLog);
                }
            } catch (Exception e) {
                log.warn("Unable to attach browser console logs for failed test.", e);
            }

            try {
                Path tracePath = manager.saveTrace(context.getDisplayName());
                if (tracePath != null && Files.exists(tracePath)) {
                    try (InputStream traceStream = Files.newInputStream(tracePath)) {
                        Allure.addAttachment("Playwright Trace" + suffix, "application/zip", traceStream, "zip");
                    }
                } else {
                    log.warn("Trace file was not created for failed test: {}", context.getDisplayName());
                }
            } catch (Exception e) {
                log.warn("Unable to save Playwright trace for failed test.", e);
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
