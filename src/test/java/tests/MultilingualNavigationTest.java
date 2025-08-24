package tests;

import com.example.testsupport.TestApplication;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.listeners.PlaywrightExtension;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.MainPage;
import com.example.testsupport.framework.device.DeviceProvider;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.example.testsupport.framework.utils.AllureHelper.step;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
class MultilingualNavigationTest {
    @Autowired private MainPage mainPage;
    @Autowired private PlaywrightManager playwrightManager;
    @Autowired private LocalizationService ls;

    @Story("Переход на страницу казино для всех поддерживаемых языков и устройств")
    @DisplayName("Навигация на страницу казино")
    @ParameterizedTest(name = "[Устройство: {0}, Язык: {1}]")
    @ArgumentsSource(DeviceProvider.class)
    void navigateToCasinoPageOnAllLanguagesAndDevices(Device device, String languageCode) {

        step("Устанавливаем размер окна просмотра", () -> {
            playwrightManager.getPage().setViewportSize(device.width(), device.height());
        });

        step("Устанавливаем язык теста", () -> {
            ls.loadLocale(languageCode);
        });
        step("Открываем главную страницу", () -> {
            mainPage.open();
        });

        step("Переходим на страницу 'Казино'", () -> {
            mainPage.navigateToCasino()
                    .verifyIsLoaded();
        });

    }
}
