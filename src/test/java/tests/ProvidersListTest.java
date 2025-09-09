package tests;

import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.device.DeviceProvider;
import com.example.testsupport.pages.MainPage;
import com.example.testsupport.pages.CasinoPage;
import com.example.testsupport.pages.ProvidersPage;
import com.example.testsupport.framework.api.client.FrontApiClient;
import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import com.example.testsupport.framework.api.dto.gambling.GamblingBrandsResponse;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.testsupport.framework.allure.Suite;

import java.util.List;

import static com.example.testsupport.framework.utils.AllureHelper.step;
import org.junit.jupiter.api.Assertions;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
@Suite("Навигация и базовый флоу казино")
class ProvidersListTest extends BaseTest {

    @Autowired private ObjectProvider<MainPage> mainPageProvider;
    @Autowired private FrontApiClient frontApiClient;

    @Story("Список провайдеров соответствует данным API")
    @DisplayName("Список провайдеров на странице совпадает с API")
    @ParameterizedTest(name = "[Устройство: {0}, Язык: {1}]")
    @ArgumentsSource(DeviceProvider.class)
    void providersMatchApi(Device device, String languageCode) {
        final class TestContext {
            MainPage mainPage;
            CasinoPage casinoPage;
            ProvidersPage providersPage;
            GamblingBrandsResponse gamblingBrandsResponse;
        }
        final TestContext ctx = new TestContext();

        step("Получаем список брендов через API", () -> {
            var params = GamblingBrandsParams.builder().build();
            ctx.gamblingBrandsResponse = frontApiClient.getGamblingBrands(languageCode, params);
        });

        step(String.format("Подготовка тестового окружения [Устройство: %s, Язык: %s]", device, languageCode), () -> {
            setupTestEnvironment(device, languageCode);
        });

        step("Открываем главную страницу", () -> {
            ctx.mainPage = mainPageProvider.getObject();
            ctx.mainPage.open();
        });

        step("Переходим на страницу провайдеров", () -> {
            ctx.casinoPage = ctx.mainPage.navigateToCasino();
            ctx.providersPage = ctx.casinoPage.navigationPanel().clickProviders();
        });

        step("Сравниваем бренды из API и интерфейса", () -> {
            List<String> apiBrands = ctx.gamblingBrandsResponse.brandNames();
            List<String> uiBrands = ctx.providersPage.getProviderNames();
            Assertions.assertIterableEquals(apiBrands, uiBrands);
        });
    }
}

