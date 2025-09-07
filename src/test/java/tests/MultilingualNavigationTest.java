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
import com.example.testsupport.framework.api.dto.gambling.GameCategory;
import com.example.testsupport.framework.api.dto.gambling.GameCategoryType;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.testsupport.framework.allure.Suite;
import java.util.List;
import java.util.stream.Stream;

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
                    .deviceType(DeviceType.MOBILE)
                    .categoryAliasArray("ww")
                    .showRestricted(true)
                    .build();

            ctx.gamblingCategoriesResponse = frontApiClient.getGamblingCategories(languageCode, params);
        });

        step("Получаем список игр через API", () -> {
            var params = GamblingGamesParams.builder()
                    .brandAliasArray("1")
                    .categoryAliasArray("2")
                    .search("123")
                    .deviceType(DeviceType.MOBILE)
                    .showRestricted(true)
                    .page(1)
                    .perPage(24)
                    .build();

            ctx.gamblingGamesResponse = frontApiClient.getGamblingGames(languageCode, params);
        });

        step("Получаем список брендов через API", () -> {
            var params = GamblingBrandsParams.builder()
                    .categoryAlias("new")
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
            List<String> apiCategories = Stream.concat(
                            ctx.gamblingCategoriesResponse.gameCategories().stream()
                                    .filter(gc -> gc.type() == GameCategoryType.ALL_GAMES)
                                    .map(GameCategory::name),
                            ctx.gamblingCategoriesResponse.gameCategories().stream()
                                    .filter(gc -> gc.type() == GameCategoryType.HORIZONTAL)
                                    .map(GameCategory::name))
                    .toList();
            List<String> uiCategories = ctx.casinoPage.getCategoryTitles();
            System.out.println("API categories: " + apiCategories);
            System.out.println("UI categories: " + uiCategories);
            Assertions.assertIterableEquals(apiCategories, uiCategories);
        });
    }
}

