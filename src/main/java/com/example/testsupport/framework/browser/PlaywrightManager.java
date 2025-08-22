package com.example.testsupport.framework.browser;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import org.springframework.stereotype.Component;

/**
 * Центральный класс для управления жизненным циклом Playwright.
 * Является Spring-бином и предоставляет готовый к использованию объект Page.
 * Обеспечивает изоляцию потоков для будущего параллельного запуска.
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
     * Инициализирует движок Playwright и создает браузер.
     * Метод безопасен для повторного вызова в одном потоке.
     */
    public void initializeBrowser() {
        if (playwright.get() == null) {
            playwright.set(Playwright.create());
        }
        if (browser.get() == null) {
            browser.set(browserFactory.create(playwright.get()));
        }
    }

    /**
     * Создает новый контекст и страницу в ранее созданном браузере.
     * Предполагается, что {@link #initializeBrowser()} уже был вызван.
     */
    public void createContextAndPage() {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1920, 1080);
        context.set(browser.get().newContext(contextOptions));
        page.set(context.get().newPage());
    }

    /**
     * Возвращает текущую страницу Playwright.
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
     * Переходит по пути относительно базового URL с учетом языка.
     *
     * @param path например, "/casino"
     */
    public void navigate(String path) {
        getPage().navigate(buildBaseUrlForCurrentLanguage() + path,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    }


    /**
     * Закрывает страницу и контекст, очищая связанные ресурсы.
     */
    public void closeContext() {
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
     * Закрывает браузер и движок Playwright.
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
     * Полностью закрывает все ресурсы Playwright.
     */
    public void closeAll() {
        closeContext();
        closeBrowser();
    }
}
