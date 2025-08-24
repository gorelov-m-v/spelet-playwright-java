package com.example.testsupport.framework.browser;

import com.microsoft.playwright.*;
import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private static final Logger log = LoggerFactory.getLogger(PlaywrightManager.class);

    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> context = new ThreadLocal<>();
    private static final ThreadLocal<Page> page = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> consoleMessages = ThreadLocal.withInitial(ArrayList::new);

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
        page.get().onConsoleMessage(msg -> consoleMessages.get().add("[" + msg.type() + "] " + msg.text()));
    }

    /**
     * Returns the current page.
     */
    public Page getPage() {
        return page.get();
    }

    public void clearConsoleMessages() {
        consoleMessages.get().clear();
    }

    public List<String> getConsoleMessages() {
        return Collections.unmodifiableList(consoleMessages.get());
    }

    private String buildBaseUrlForCurrentLanguage() {
        String lang = ls.getCurrentLangCode();
        if (lang == null || lang.equals(props.getDefaultLanguage())) {
            return props.getBaseUrl();
        }
        return props.getBaseUrl() + "/" + lang;
    }

    public void open() {
        getPage().navigate(buildBaseUrlForCurrentLanguage());
    }

    /**
     * Navigates to a path relative to the base URL considering the language.
     *
     * @param path e.g., "/casino"
     */
    public void navigate(String path) {
        getPage().navigate(buildBaseUrlForCurrentLanguage() + path);
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
            context.get().close();
            context.remove();
        }
    }

    /**
     * Stops tracing and saves the trace file for a failed test.
     *
     * @param testName The display name of the test, used for the filename.
     * @return The path to the saved trace file, or {@code null} if saving failed.
     */
    public Path saveTrace(String testName) {
        String sanitizedTestName = testName
                .replaceAll("[^a-zA-Z0-9.-]", "_")
                .replaceAll("\\s+", "_");

        String traceName = "trace-" + sanitizedTestName + ".zip";
        Path tracePath = Paths.get("build", "traces", traceName);

        if (context.get() != null) {
            try {
                Files.createDirectories(tracePath.getParent());
                context.get().tracing().stop(new Tracing.StopOptions().setPath(tracePath));
                log.info("Playwright trace saved to: {}", tracePath);
                return tracePath;
            } catch (Exception e) {
                log.warn("Failed to save Playwright trace file for test: {}", testName, e);
            }
        }
        return null;
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
