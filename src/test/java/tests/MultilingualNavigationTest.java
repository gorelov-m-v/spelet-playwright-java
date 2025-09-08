package tests;

import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.device.DeviceProvider;
import com.example.testsupport.pages.MainPage;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.ObjectProvider;
import com.example.testsupport.pages.CasinoPage;
import com.example.testsupport.framework.api.client.FrontApiClient;
import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import com.example.testsupport.framework.api.client.params.GamblingCategoriesParams;
import com.example.testsupport.framework.api.client.params.GamblingGamesParams;
import com.example.testsupport.framework.api.client.params.DeviceType;
import com.example.testsupport.framework.api.dto.gambling.GamblingBrandsResponse;
import com.example.testsupport.framework.api.dto.gambling.GamblingCategoriesResponse;
import com.example.testsupport.framework.api.dto.gambling.GamblingGamesResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.testsupport.framework.allure.Suite;
import java.util.List;

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
            GamblingCategoriesResponse gamblingCategoriesResponse;
            GamblingGamesResponse gamblingGamesResponse;
            GamblingBrandsResponse gamblingBrandsResponse;
        }
        final TestContext ctx = new TestContext();

        step("Получаем список категорий игр через API", () -> {
            var params = GamblingCategoriesParams.builder()
                    .build();

            ctx.gamblingCategoriesResponse = frontApiClient.getGamblingCategories(languageCode, params);
        });

        step("Получаем список игр через API", () -> {
            var params = GamblingGamesParams.builder()
                    .build();

            ctx.gamblingGamesResponse = frontApiClient.getGamblingGames(languageCode, params);
        });

        step("Получаем список брендов через API", () -> {
            var params = GamblingBrandsParams.builder()
                    .build();

            ctx.gamblingBrandsResponse = frontApiClient.getGamblingBrands(languageCode, params);
        });

        step(String.format("Подготовка тестового окружения [Устройство: %s, Язык: %s]", device, languageCode), () -> {
            setupTestEnvironment(device, languageCode);
        });

        step("Открываем главную страницу", () -> {
            ctx.mainPage = mainPageProvider.getObject();
            ctx.mainPage.open();
        });

        step("Переходим на страницу 'Казино'", () -> {
            ctx.casinoPage = ctx.mainPage.navigateToCasino();
        });

        step("Сравниваем категории из API и интерфейса", () -> {
            List<String> apiCategories = ctx.gamblingCategoriesResponse.horizontalCategoryNamesWithLobby(ls);
            List<String> uiCategories = ctx.casinoPage.categoryTabs().getTitles();

            Assertions.assertIterableEquals(apiCategories, uiCategories);
        });

        step("Сравниваем навигационные категории из API и интерфейса", () -> {
            List<String> apiNavigationCategories = ctx.gamblingCategoriesResponse.navigationCategoryNames();
            List<String> uiNavigationCategories = ctx.casinoPage.navigationPanel().getTitles();

            Assertions.assertIterableEquals(apiNavigationCategories, uiNavigationCategories);
        });

        step("Сравниваем категории из API и фильтров", () -> {
            List<String> apiFilterCategories = ctx.gamblingCategoriesResponse.horizontalCategoryNames();
            List<String> uiFilterCategories = ctx.casinoPage.openFilters()
                    .verifyIsLoaded()
                    .getCategoryNames();

            Assertions.assertIterableEquals(apiFilterCategories, uiFilterCategories);
        });

        step("Сравниваем бренды из API и фильтров", () -> {
            List<String> apiBrands = ctx.gamblingBrandsResponse.brandNames();
            List<String> uiBrands = ctx.casinoPage.openFilters()
                    .verifyIsLoaded()
                    .getProviderNames();

            Assertions.assertIterableEquals(apiBrands, uiBrands);
        });
    }
}

