package tests;

import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.device.DeviceProvider;
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
import com.example.testsupport.framework.api.dto.gambling.Brand;
import com.example.testsupport.framework.api.dto.gambling.GamblingGamesResponse;
import java.util.concurrent.ThreadLocalRandom;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
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
    @ParameterizedTest(name = "[Устройство: {0}, Язык: {1}]")
    @ArgumentsSource(DeviceProvider.class)
    void navigateToCasinoPageOnAllLanguagesAndDevices(Device device, String languageCode) {

        final class TestContext {
            MainPage mainPage;
            CasinoPage casinoPage;
            FilterDrawerComponent filterDrawer;
            AuthModalComponent authModal;
            GamblingBrandsResponse gamblingBrandsResponse;
             GamblingGamesResponse gamblingGamesResponse;
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

        Brand randomBrand = step("Выбираем случайный бренд", () -> {
            var brands = ctx.gamblingBrandsResponse.brands();
            var brand = brands.get(ThreadLocalRandom.current().nextInt(brands.size()));
            ctx.filterDrawer.selectProvider(brand.name());
            return brand;
        });

        step("Получаем список игр бренда", () -> {
              var response = frontApiClient.getGamblingGames(randomBrand.alias());
            Assertions.assertEquals(200, response.getStatusCode().value());
            ctx.gamblingGamesResponse = response.getBody();
        });

        step("Применяем фильтры", () -> {
            ctx.casinoPage = ctx.filterDrawer.clickShow()
                    .verifyIsLoaded();
        });

        String firstGameName = step("Ищем первую игру из списка", () -> {
            var name = ctx.gamblingGamesResponse.games().getFirst().name();
            ctx.casinoPage.typeInSearch(name)
                    .waitForGameVisible(name);
            return name;
        });

        step("Запускаем игру '" + firstGameName + "'", () -> {
            ctx.authModal = ctx.casinoPage.clickPlay(firstGameName)
                    .verifyIsLoaded();
        });
    }
}

