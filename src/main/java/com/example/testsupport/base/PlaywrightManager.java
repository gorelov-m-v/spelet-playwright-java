package com.example.testsupport.base;

import com.example.testsupport.config.AppProperties;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Component;

/**
 * Центральный класс для управления жизненным циклом Playwright.
 * Является Spring-бином и предоставляет готовый к использованию объект Page.
 * Обеспечивает изоляцию потоков для будущего параллельного запуска.
 */
@Component
public class PlaywrightManager {

    private final AppProperties props;
    private final BrowserStackClient bsClient;

    // ThreadLocal обеспечивает, что у каждого потока выполнения (теста) будет свой собственный экземпляр
    // Playwright, Browser и Page. Это критически важно для параллельного запуска.
    private static final ThreadLocal<Playwright> playwright = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browser = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> context = new ThreadLocal<>();
    private static final ThreadLocal<Page> page = new ThreadLocal<>();

    // Spring автоматически внедрит сюда бин AppProperties при создании base.PlaywrightManager
    public PlaywrightManager(AppProperties props) {
        this.props = props;
        this.bsClient = new BrowserStackClient();
    }

    /**
     * Главный метод. Предоставляет готовый к работе объект Page.
     * Если для текущего потока он еще не создан - инициализирует всю цепочку Playwright.
     * @return экземпляр Page для текущего теста.
     */
    public Page getPage() {
        if (page.get() == null) {
            playwright.set(Playwright.create());
            browser.set(createBrowser());

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080); // Задаем стандартный размер окна
            context.set(browser.get().newContext(contextOptions));

            page.set(context.get().newPage());
        }
        return page.get();
    }

    /**
     * Вспомогательный метод для навигации. Скрывает детали реализации от теста.
     * @param url URL для перехода.
     */
    public void navigate(String url) {
        getPage().navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
    }

    /**
     * Внутренний метод, который решает, какой браузер создать: локальный или в BrowserStack.
     * @return созданный экземпляр Browser.
     */
    private Browser createBrowser() {
        RunMode runMode = RunMode.fromProperty(props.getRemote());
        String browserName = props.getBrowser().toLowerCase();

        if (runMode == RunMode.BROWSERSTACK) {
            return bsClient.connectBrowser(playwright.get());
        } else {
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setHeadless(props.isHeadless());

            return switch (browserName) {
                case "firefox" -> playwright.get().firefox().launch(options);
                case "webkit"  -> playwright.get().webkit().launch(options);
                default        -> playwright.get().chromium().launch(options);
            };
        }
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