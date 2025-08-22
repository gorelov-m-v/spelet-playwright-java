package tests;

import com.example.testsupport.TestApplication;
import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.listeners.PlaywrightExtension;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.CasinoPage;
import com.example.testsupport.pages.MainPage;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static io.qameta.allure.Allure.step;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
class MultilingualNavigationTest {
    @Autowired private MainPage mainPage;
    @Autowired private CasinoPage casinoPage;
    @Autowired private AppProperties props;
    @Autowired private PlaywrightManager playwrightManager;
    @Autowired private LocalizationService ls;

    static Stream<String> languageProvider() {
        return Stream.of("lv", "ru", "en");
    }

    @Story("Переход на страницу казино для всех поддерживаемых языков")
    @DisplayName("Навигация на страницу казино для языка:")
    @ParameterizedTest(name = "[Язык: {0}]")
    @MethodSource("languageProvider")
    void navigateToCasinoPageOnAllLanguages(String languageCode) {

        step("Установить язык теста: " + languageCode, () -> {
            props.setLanguage(languageCode);
            ls.loadLocale(languageCode);
        });

        step("Открыть главную страницу", () -> {
            playwrightManager.open();
        });

        step("Переход на страницу казино", () -> {
            mainPage.clickCasino();
        });

        step("Проверить, что URL содержит " + casinoPage.getExpectedPath(), () -> {
            casinoPage.verifyUrl();
        });

    }
}
