package tests;

import com.example.testsupport.framework.device.Device;
import com.example.testsupport.pages.MainPage;
import com.example.testsupport.framework.device.DeviceProvider;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;

import static com.example.testsupport.framework.utils.AllureHelper.step;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
class MultilingualNavigationTest extends BaseTest {
    @Autowired private MainPage mainPage;

    @Story("Переход на страницу казино для всех поддерживаемых языков и устройств")
    @DisplayName("Навигация на страницу казино")
    @ParameterizedTest(name = "[Устройство: {0}, Язык: {1}]")
    @ArgumentsSource(DeviceProvider.class)
    void navigateToCasinoPageOnAllLanguagesAndDevices(Device device, String languageCode) {

        step(String.format("Подготовка тестового окружения [Устройство: %s, Язык: %s]", device, languageCode), () -> {
            setupTestEnvironment(device, languageCode);
        });

        step("Открываем главную страницу", () -> {
            mainPage.open()
                    .verifyIsLoaded();
        });

        step("Переходим на страницу 'Казино'", () -> {
            mainPage.navigateToCasino()
                    .verifyIsLoaded();
        });
    }
}
