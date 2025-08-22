package com.example.testsupport.framework.listeners;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.lifecycle.PlaywrightLifecycleStrategy;

/**
 * Расширение JUnit 5, которое управляет жизненным циклом Playwright
 * и создает полезные вложения Allure при падении теста.
 */
public class PlaywrightExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightExtension.class);

    private ApplicationContext ctx;
    private PlaywrightLifecycleStrategy lifecycle;

    @Override
    public void beforeAll(ExtensionContext context) {
        ctx = SpringExtension.getApplicationContext(context);
        lifecycle = ctx.getBean(PlaywrightLifecycleStrategy.class);
        lifecycle.beforeAll(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        LocalizationService ls = ctx.getBean(LocalizationService.class);
        AppProperties props = ctx.getBean(AppProperties.class);
        ls.loadLocale(props.getLanguage());

        lifecycle.beforeEach(context);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        PlaywrightManager manager = ctx.getBean(PlaywrightManager.class);
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
            lifecycle.afterEach(context);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        lifecycle.afterAll(context);
    }
}

