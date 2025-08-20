package tests;

import com.example.testsupport.base.PlaywrightManager;
import com.example.testsupport.TestApplication;
import com.example.testsupport.config.AppProperties;
import com.microsoft.playwright.Page;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pages.MainPage;

import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
@SpringBootTest(classes = TestApplication.class)
class SpeletCasinoTest {

    // Внедряем зависимости через Spring. Никакого ручного создания!
    @Autowired
    private PlaywrightManager playwrightManager;

    @Autowired
    private AppProperties props;

    // В `@BeforeEach` теперь только один вызов для инициализации.
    @BeforeEach
    void setUp() {
        playwrightManager.getPage();
    }

    // В `@AfterEach` теперь только один вызов для закрытия.
    // Логику Allure пока оставим здесь, на следующем шаге мы ее тоже вынесем.
    @AfterEach
    void tearDown() {
        Page page = playwrightManager.getPage();
        try {
            Allure.addAttachment("Current URL", "text/plain", page.url());
            Allure.addAttachment("Page HTML", "text/html", page.content(), ".html");
            byte[] png = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            Allure.getLifecycle().addAttachment("Screenshot", "image/png", "png", png);
        } catch (Throwable ignored) {}

        playwrightManager.close();
    }

    @Story("Переход на страницу казино")
    @DisplayName("Клик по «Kazino» ведёт на /casino")
    @Test
    void navigateToCasinoFromHome() {
        // Получаем Page из менеджера. Теперь это единственный источник.
        Page page = playwrightManager.getPage();

        // Шаг 1: открыть главную
        step("Открыть главную страницу: " + props.getBaseUrl(), () ->
                // Используем наш удобный метод navigate
                playwrightManager.navigate(props.getBaseUrl())
        );

        // Шаг 2: клик по «Kazino»
        step("Клик по пункту меню «Kazino»", () -> {
            MainPage mainPage = new MainPage(page);
            mainPage.clickKazino();
        });

        // Шаг 3: верифицировать URL
        step("Проверить, что URL содержит /casino", () -> {
            String current = page.url();
            assertTrue(current.contains("/casino"),
                    "Ожидали переход на страницу /casino, фактический: " + current);
        });
    }
}
