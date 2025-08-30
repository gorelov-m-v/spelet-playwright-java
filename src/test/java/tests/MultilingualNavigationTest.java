package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("Spelet.lv")
@Feature("Навигация по шапке")
class MultilingualNavigationTest extends BaseTest {
    @Test
    @DisplayName("temporary debug test")
    void temporaryDebugTest() {
        System.out.println("Context is up!");
        checkConfig();
    }
}
