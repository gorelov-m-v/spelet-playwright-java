package tests;

import com.example.testsupport.TestApplication;
import com.example.testsupport.base.PlaywrightExtension;
import com.example.testsupport.base.PlaywrightManager;
import com.example.testsupport.config.AppProperties;
import com.microsoft.playwright.Page;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pages.MainPage;

import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
class SpeletCasinoTest {

    @Autowired
    private PlaywrightManager playwrightManager;

    @Autowired
    private AppProperties props;

    @Story("Переход на страницу казино")
    @DisplayName("Клик по «Kazino» ведёт на /casino")
    @Test
    void navigateToCasinoFromHome() {
        Page page = playwrightManager.getPage();

        step("Открыть главную страницу: " + props.getBaseUrl(), () ->
                playwrightManager.navigate(props.getBaseUrl())
        );

        step("Клик по пункту меню «Kazino»", () -> {
            MainPage mainPage = new MainPage(page);
            mainPage.clickKazino();
        });

        step("Проверить, что URL содержит /casino", () -> {
            String current = page.url();
            assertTrue(current.contains("/casino"),
                    "Ожидали переход на страницу /casino, фактический: " + current);
        });
    }
}
