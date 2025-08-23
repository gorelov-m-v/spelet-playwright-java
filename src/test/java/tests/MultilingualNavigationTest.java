package tests;

import com.example.testsupport.TestApplication;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.listeners.PlaywrightExtension;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.MainPage;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.stream.Stream;

import static com.example.testsupport.framework.utils.AllureHelper.step;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
class MultilingualNavigationTest {
    @Autowired private MainPage mainPage;
    @Autowired private PlaywrightManager playwrightManager;
    @Autowired private LocalizationService ls;

    static Stream<Arguments> deviceAndLanguageProvider() {
        List<String> languages = List.of("lv", "ru", "en");
        List<Device> devices = List.of(
                new Device("Desktop", 1920, 1080),
                new Device("Mobile", 390, 844)
        );

        return devices.stream()
                .flatMap(device -> languages.stream().map(lang -> Arguments.of(device, lang)));
    }

    @Story("Переход на страницу казино для всех поддерживаемых языков и устройств")
    @DisplayName("Навигация на страницу казино")
    @ParameterizedTest(name = "[Устройство: {0}, Язык: {1}]")
    @MethodSource("deviceAndLanguageProvider")
    void navigateToCasinoPageOnAllLanguagesAndDevices(Device device, String languageCode) {

        step("Устанавливаем размер окна просмотра", () -> {
            playwrightManager.getPage().setViewportSize(device.width(), device.height());
        });

        step("Устанавливаем язык теста", () -> {
            ls.loadLocale(languageCode);
        });

        step("Открыть главную страницу", () -> {
            playwrightManager.open();
        });

        step("Перейти на страницу казино и проверить URL", () -> {
            mainPage.navigateToCasino().verifyUrl();
        });

    }
}
