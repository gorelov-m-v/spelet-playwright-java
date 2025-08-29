package tests;

import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.device.DeviceProvider;
import com.example.testsupport.pages.MainPage;
import com.example.testsupport.pages.CasinoPage;
import com.example.testsupport.pages.components.FilterDrawerComponent;
import com.example.testsupport.pages.components.AuthModalComponent;
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

        final class TestContext {
            CasinoPage casinoPage;
            FilterDrawerComponent filterDrawer;
            AuthModalComponent authModal;
        }
        final TestContext ctx = new TestContext();

        step("Открываем главную страницу", () -> {
            mainPage.open()
                    .verifyIsLoaded();
        });

        step("Переходим на страницу 'Казино'", () -> {
            ctx.casinoPage = mainPage.navigateToCasino()
                    .verifyIsLoaded();
        });

        step("Открываем дровер фильтров", () -> {
            ctx.filterDrawer = ctx.casinoPage.openFilters()
                    .verifyIsLoaded();
        });

        step("Выбираем провайдера 'Play'n Go'", () -> {
            ctx.filterDrawer.selectProvider("Play'n Go");
        });

        step("Применяем фильтры", () -> {
            ctx.casinoPage = ctx.filterDrawer.clickShow()
                    .verifyIsLoaded();
        });

        step("Ищем игру 'Book of Dead'", () -> {
            ctx.casinoPage.typeInSearch("Book of Dead")
                    .waitForGameVisible("Book of Dead");
        });

        step("Запускаем игру 'Book of Dead'", () -> {
            ctx.authModal = ctx.casinoPage.clickPlay("Book of Dead")
                    .verifyIsLoaded();
        });
    }
}
