package com.example.testsupport.framework.listeners;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.browser.PlaywrightManager;

/**
 * Расширение JUnit 5, которое управляет жизненным циклом Playwright
 * и создает полезные вложения Allure при падении теста.
 */
public class PlaywrightExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(context);
        LocalizationService ls = ctx.getBean(LocalizationService.class);
        AppProperties props = ctx.getBean(AppProperties.class);
        ls.loadLocale(props.getLanguage());

        PlaywrightManager manager = ctx.getBean(PlaywrightManager.class);
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

