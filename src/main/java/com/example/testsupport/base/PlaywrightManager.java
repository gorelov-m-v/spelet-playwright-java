package com.example.testsupport.base;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.example.testsupport.config.AppProperties;
import com.example.testsupport.localization.LocalizationService;
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

    // ThreadLocal обеспечивает, что у каждого потока выполнения (теста) будет свой собственный экземпляр
    // Playwright, Browser и Page. Это критически важно для параллельного запуска.
    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> context = new ThreadLocal<>();
    private static final ThreadLocal<Page> page = new ThreadLocal<>();

    // Spring автоматически внедрит сюда реализацию BrowserFactory в соответствии с активным профилем
    public PlaywrightManager(BrowserFactory browserFactory,
                             AppProperties props,
                             LocalizationService ls) {
        this.browserFactory = browserFactory;
        this.props = props;
        this.ls = ls;
    }

    /**
     * Главный метод. Предоставляет готовый к работе объект Page.
     * Если для текущего потока он еще не создан - инициализирует всю цепочку Playwright.
     * @return экземпляр Page для текущего теста.
     */
    public Page getPage() {
        if (page.get() == null) {
            playwright.set(Playwright.create());
            browser.set(browserFactory.create(playwright.get()));

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080); // Задаем стандартный размер окна
            context.set(browser.get().newContext(contextOptions));

            page.set(context.get().newPage());
        }
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
     * Navigates to a path relative to the language-aware base URL.
     * @param path e.g. "/casino"
     */
    public void navigate(String path) {
        getPage().navigate(buildBaseUrlForCurrentLanguage() + path,
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    }


    /**
     * Закрывает все ресурсы Playwright в правильном порядке и очищает ThreadLocal переменные.
     * Важно вызывать после каждого теста, чтобы не оставлять "висящих" процессов.
     */
    public void close() {
        if (page.get() != null) {
            page.get().close();
            page.remove();
        }
        if (context.get() != null) {
            context.get().close();
            context.remove();
        }
        if (browser.get() != null) {
            browser.get().close();
            browser.remove();
        }
        if (playwright.get() != null) {
            playwright.get().close();
            playwright.remove();
        }
    }
}