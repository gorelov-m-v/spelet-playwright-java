package com.example.testsupport.pages;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Page;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.example.testsupport.framework.utils.AllureHelper.step;
import static com.example.testsupport.framework.utils.Breakpoints.MOBILE;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MainPage extends BasePage<MainPage> {

    private final ObjectProvider<CasinoPage> casinoPageProvider;

    public MainPage(ObjectProvider<Page> page,
                    LocalizationService ls,
                    ObjectProvider<CasinoPage> casinoPageProvider,
                    AppProperties props) {
        super(page, ls, props);
        this.casinoPageProvider = casinoPageProvider;
    }

    /**
     * Navigates to the casino page through the menu, adapting to screen size.
     */
    @SuppressWarnings("resource")
    public CasinoPage navigateToCasino() {
        return step("Навигация на страницу 'Казино'", () -> {
            int currentWidth = page().viewportSize().width;
            if (currentWidth < MOBILE) {
                tabBar().clickCasino();
            } else {
                header().clickCasino();
            }
            step("Ожидание URL страницы 'Казино'", () -> page().waitForURL("**/casino"));
            return casinoPageProvider.getObject().verifyIsLoaded();
        });
    }

    /**
     * Verifies that the main page is loaded.
     *
     * @return current page object
     */
    @Override
    public MainPage verifyIsLoaded() {
        return step("Проверка загрузки главной страницы", () -> {
            header().verifyLogoVisible();
            verifyUrlContains("/");
            return this;
        });
    }

    /**
     * Opens the main page and verifies it's loaded.
     *
     * @return current page object
     */
    public MainPage open() {
        return step("Открыть главную страницу", () -> {
            super.open();
            return verifyIsLoaded();
        });
    }
}

