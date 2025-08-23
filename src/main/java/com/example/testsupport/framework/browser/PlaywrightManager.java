package com.example.testsupport.framework.browser;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Central class managing the Playwright lifecycle.
 * Registered as a Spring bean and provides a ready-to-use {@link Page} instance.
 * Ensures thread isolation for future parallel runs.
 */
@Component
public class PlaywrightManager {

    private final BrowserFactory browserFactory;
    private final AppProperties props;
    private final LocalizationService ls;

    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> context = new ThreadLocal<>();
    private static final ThreadLocal<Page> page = new ThreadLocal<>();

    public PlaywrightManager(BrowserFactory browserFactory,
                             AppProperties props,
                             LocalizationService ls) {
        this.browserFactory = browserFactory;
        this.props = props;
        this.ls = ls;
    }

    /**
     * Initializes the Playwright engine and browser once per thread.
     */
    public void initializeBrowser() {
        if (playwright.get() == null) {
            playwright.set(Playwright.create());
            browser.set(browserFactory.create(playwright.get()));
        }
    }

    /**
     * Creates a new browser context and page for the current test.
     */
    public void createContextAndPage() {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080);
        context.set(browser.get().newContext(contextOptions));
        context.get().tracing().start(new Tracing.StartOptions()
                .setTitle("trace")
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        page.set(context.get().newPage());
    }

    /**
     * Returns the current page.
     */
    public Page getPage() {
        return page.get();
    }

    private String buildBaseUrlForCurrentLanguage() {
        String lang = ls.getCurrentLangCode();
        if (lang == null || lang.equals(props.getDefaultLanguage())) {
            return props.getBaseUrl();
        }
        return props.getBaseUrl() + "/" + lang;
    }

    public void open() {
        getPage().navigate(buildBaseUrlForCurrentLanguage(),
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    }

    /**
     * Navigates to a path relative to the base URL considering the language.
     *
     * @param path e.g., "/casino"
     */
    public void navigate(String path) {
        getPage().navigate(buildBaseUrlForCurrentLanguage() + path,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    }
    /**
     * Closes the page and context.
     */
    public void closeContextAndPage() {
        if (page.get() != null) {
            page.get().close();
            page.remove();
        }
        if (context.get() != null) {
            try {
                Path tracesDir = Paths.get("build", "traces");
                Files.createDirectories(tracesDir);
                String traceName = "trace-" + Thread.currentThread().getId() + "-" + System.nanoTime() + ".zip";
                context.get().tracing().stop(new Tracing.StopOptions()
                        .setPath(tracesDir.resolve(traceName)));
            } catch (Exception ignored) {
                // ignore failures during trace saving
            }
            context.get().close();
            context.remove();
        }
    }

    /**
     * Closes the browser and Playwright engine.
     */
    public void closeBrowser() {
        if (browser.get() != null) {
            browser.get().close();
            browser.remove();
        }
        if (playwright.get() != null) {
            playwright.get().close();
            playwright.remove();
        }
    }

    /**
     * Closes all Playwright resources. Kept for backward compatibility.
     */
    public void close() {
        closeContextAndPage();
        closeBrowser();
    }
}
