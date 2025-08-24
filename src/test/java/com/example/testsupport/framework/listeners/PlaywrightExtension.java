package com.example.testsupport.framework.listeners;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.browser.BrowserStackSessionManager;
import com.example.testsupport.framework.browser.BrowserStackTestReporter;
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
    private BrowserStackSessionManager sessionManager;
    private static BrowserStackTestReporter reporter;

    @Override
    public void beforeAll(ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        lifecycle = ctx.getBean(PlaywrightLifecycleStrategy.class);
        lifecycle.beforeAll();
        reporter = new BrowserStackTestReporter();
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

        if (sessionManager == null) {
            sessionManager = ctx.getBean(BrowserStackSessionManager.class);
        }

        PlaywrightManager manager = ctx.getBean(PlaywrightManager.class);
        manager.clearConsoleMessages();

        String testName = context.getDisplayName();
        manager.initializeBrowser(testName);
        manager.createContextAndPage();
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

                // 2. Attach browser console logs
                String consoleLog = String.join(System.lineSeparator(), manager.getConsoleMessages());
                if (!consoleLog.isEmpty()) {
                    Allure.addAttachment("Browser Console Logs", "text/plain", consoleLog);
                }

                // 3. Save and attach Playwright Trace
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

        String sessionId = sessionManager != null ? sessionManager.getSessionId() : null;
        if (sessionId != null) {
            if (context.getExecutionException().isPresent()) {
                String reason = context.getExecutionException().get().getMessage();
                reporter.testFailed(sessionId, reason);
            } else {
                reporter.testPassed(sessionId);
            }
        }

        // Finally, always run the lifecycle cleanup
        try {
            lifecycle.afterEach();
        } finally {
            if (sessionManager != null) {
                sessionManager.clear();
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (lifecycle != null) {
            lifecycle.afterAll();
        }
    }
}

