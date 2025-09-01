package tests;

import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.junit.retries.RetryableParameterizedTest;
import com.example.testsupport.pages.MainPage;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.ObjectProvider;
import com.example.testsupport.pages.CasinoPage;
import com.example.testsupport.pages.components.FilterDrawerComponent;
import com.example.testsupport.pages.components.AuthModalComponent;
import com.example.testsupport.framework.api.client.FrontApiClient;
import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import com.example.testsupport.framework.api.dto.gambling.GamblingBrandsResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.testsupport.framework.allure.Suite;

import static com.example.testsupport.framework.utils.AllureHelper.step;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
@Suite("Навигация и базовый флоу казино")
@Execution(ExecutionMode.CONCURRENT)
class MultilingualNavigationTest extends BaseTest {
    @Autowired private ObjectProvider<MainPage> mainPageProvider;
    @Autowired private FrontApiClient frontApiClient;

    @Story("Переход на страницу казино для всех поддерживаемых языков и устройств")
    @DisplayName("Навигация на страницу казино")
    @RetryableParameterizedTest(name = "[Устройство: {0}, Язык: {1}]")
    void navigateToCasinoPageOnAllLanguagesAndDevices(Device device, String languageCode) {

        final class TestContext {
            MainPage mainPage;
            CasinoPage casinoPage;
            FilterDrawerComponent filterDrawer;
            AuthModalComponent authModal;
            GamblingBrandsResponse gamblingBrandsResponse;
        }
        final TestContext ctx = new TestContext();

        step("Получаем список брендов через API", () -> {
            var params = GamblingBrandsParams.builder()
                    .platformLocale(languageCode)
                    .categoryAlias("new")
                    .build();

            ctx.gamblingBrandsResponse = frontApiClient.getGamblingBrands(params);
        });

        step(String.format("Подготовка тестового окружения [Устройство: %s, Язык: %s]", device, languageCode), () -> {
            setupTestEnvironment(device, languageCode);
        });

        step("Открываем главную страницу", () -> {
            ctx.mainPage = mainPageProvider.getObject();
            ctx.mainPage.open().verifyIsLoaded();
        });

        step("Переходим на страницу 'Казино'", () -> {
            ctx.casinoPage = ctx.mainPage.navigateToCasino().verifyIsLoaded();
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

