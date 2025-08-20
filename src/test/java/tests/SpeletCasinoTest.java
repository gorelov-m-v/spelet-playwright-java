package tests;

import com.example.testsupport.TestApplication;
import com.example.testsupport.base.PlaywrightExtension;
import com.example.testsupport.base.PlaywrightManager;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.testsupport.pages.MainPage;

import static io.qameta.allure.Allure.step;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
class SpeletCasinoTest {

    @Autowired
    private PlaywrightManager playwrightManager;

    @Autowired
    private MainPage mainPage;

    @Story("Переход на страницу казино")
    @DisplayName("Клик по «Казино» ведёт на /casino")
    @Test
    void navigateToCasinoFromHome() {
        step("Открыть главную страницу", () -> playwrightManager.open());
        step("Клик по пункту меню «Казино»", mainPage::clickCasino);
        step("Проверить, что URL содержит /casino", () ->
                mainPage.verifyUrlContains("/casino")
        );
    }
}
