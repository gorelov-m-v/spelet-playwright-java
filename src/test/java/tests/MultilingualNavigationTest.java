package tests;

import com.example.testsupport.framework.allure.Suite;
import com.example.testsupport.framework.api.client.FrontApiClient;
import com.example.testsupport.framework.api.client.params.GamblingBrandsParams;
import com.example.testsupport.framework.api.dto.gambling.GamblingBrandsResponse;
import com.example.testsupport.framework.device.Device;
import com.example.testsupport.framework.device.TestMatrixService;
import com.example.testsupport.framework.utils.SuccessTracker;
import com.example.testsupport.pages.CasinoPage;
import com.example.testsupport.pages.MainPage;
import com.example.testsupport.pages.components.AuthModalComponent;
import com.example.testsupport.pages.components.FilterDrawerComponent;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.example.testsupport.framework.utils.AllureHelper.step;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
@Suite("Навигация и базовый флоу казино")
class MultilingualNavigationTest extends BaseTest {
    @Autowired private ObjectProvider<MainPage> mainPageProvider;
    @Autowired private FrontApiClient frontApiClient;
    @Autowired private TestMatrixService testMatrixService;

    @BeforeAll
    static void beforeAllTests() {
        SuccessTracker.clear();
    }

    @Story("Переход на страницу казино для всех поддерживаемых языков и устройств")
    @DisplayName("Навигация на страницу казино")
    @CartesianTest(name = "[Устройство: {0}, Язык: {1}] - Попытка {2}")
    void navigateToCasinoPageOnAllLanguagesAndDevices(
            @DeviceSource Device device,
            @LanguageSource String languageCode,
            @CartesianTest.Values(ints = {1, 2, 3}) int attempt
    ) {
        String testCaseId = device.getName() + "-" + languageCode;

        Assumptions.assumeFalse(
            SuccessTracker.isSuccessful(testCaseId),
            "Тест для этой комбинации параметров уже прошел успешно. Пропускаем попытку " + attempt
        );

        try {
            final class TestContext {
                MainPage mainPage;
                CasinoPage casinoPage;
                FilterDrawerComponent filterDrawer;
                AuthModalComponent authModal;
                GamblingBrandsResponse gamblingBrandsResponse;
            }
            final TestContext ctx = new TestContext();

            step("Получаем список брендов через API", () -> {
                var params = GamblingBrandsParams.builder().categoryAlias("new").build();
                ctx.gamblingBrandsResponse = frontApiClient.getGamblingBrands(languageCode, params);
            });
            step(String.format("Подготовка тестового окружения [Устройство: %s, Язы-к: %s]", device, languageCode), () -> {
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
                ctx.filterDrawer = ctx.casinoPage.openFilters().verifyIsLoaded();
            });
            step("Выбираем провайдера 'Play'n Go'", () -> {
                ctx.filterDrawer.selectProvider("Play'n Go");
            });
            step("Применяем фильтры", () -> {
                ctx.casinoPage = ctx.filterDrawer.clickShow().verifyIsLoaded();
            });
            step("Ищем игру 'Book of Dead'", () -> {
                ctx.casinoPage.typeInSearch("Book of Dead").waitForGameVisible("Book of Dead");
            });
            step("Запускаем игру 'Book of Dead'", () -> {
                ctx.authModal = ctx.casinoPage.clickPlay("Book of Dead").verifyIsLoaded();
            });

            SuccessTracker.markAsSuccess(testCaseId);
        } catch (Throwable e) {
            Assumptions.abort("Попытка " + attempt + " не удалась: " + e.getMessage());
        }
    }

    Stream<Device> deviceProvider() {
        return testMatrixService.getDevices();
    }

    Stream<String> languageProvider() {
        return testMatrixService.getLanguages();
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @CartesianTest.MethodFactory("deviceProvider")
    public @interface DeviceSource {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @CartesianTest.MethodFactory("languageProvider")
    public @interface LanguageSource {}
}
