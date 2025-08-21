package tests;

import com.example.testsupport.TestApplication;
import com.example.testsupport.framework.browser.PlaywrightManager;
import com.example.testsupport.framework.data.CasinoDataRegistry;
import com.example.testsupport.framework.listeners.PlaywrightExtension;
import com.example.testsupport.framework.routing.PageAsserter;
import com.example.testsupport.ui.pages.CasinoPage;
import io.qameta.allure.*;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.qameta.allure.Allure.step;

/**
 * Пример теста, демонстрирующего использование DataRegistry и PageAsserter
 * для построения и проверки URL без хардкода алиасов.
 */
@SpringBootTest(classes = TestApplication.class)
@ExtendWith(PlaywrightExtension.class)
@Epic("Spelet.lv")
@Feature("Фильтрация казино")
class FilterTest {

    @Autowired private PlaywrightManager playwrightManager;
    @Autowired private CasinoDataRegistry registry;
    @Autowired private PageAsserter asserter;

    @Test
    @DisplayName("Открытие страницы казино с фильтром по бренду")
    void openCasinoPageWithBrandFilter() {
        String alias = step("Получить алиас бренда из API",
                () -> registry.getBrandAliasByName("Some Brand"));

        step("Открыть страницу казино с query-параметром",
                () -> playwrightManager.open(CasinoPage.class, Map.of("brand", alias)));

        step("Проверить URL и наличие параметра", () -> {
            asserter.amOn(CasinoPage.class);
            asserter.urlContainsQueryParam("brand", alias);
        });
    }
}
