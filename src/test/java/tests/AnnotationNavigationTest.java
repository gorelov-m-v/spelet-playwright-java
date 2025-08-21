package tests;

import com.example.testsupport.TestApplication;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.listeners.PlaywrightExtension;
import com.example.testsupport.framework.routing.PageAsserter;
import com.example.testsupport.ui.pages.CasinoPage;
import com.example.testsupport.ui.pages.MainPage;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.qameta.allure.Allure.step;

@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
@Epic("Spelet.lv")
@Feature("Роутинг через аннотации")
class AnnotationNavigationTest {

    @Autowired private PlaywrightManager playwrightManager;
    @Autowired private MainPage mainPage;
    @Autowired private PageAsserter asserter;

    @Test
    @DisplayName("Переход с главной страницы на страницу казино")
    void navigationFromMainToCasinoPage() {
        step("Открыть главную страницу", () ->
            playwrightManager.open(MainPage.class)
        );

        step("Перейти на страницу казино через меню", () ->
            mainPage.clickCasino()
        );

        step("Проверить, что мы находимся на странице казино", () ->
            asserter.amOn(CasinoPage.class)
        );
    }
}
