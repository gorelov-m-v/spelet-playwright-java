package com.example.testsupport.pages;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Page;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MainPage extends BasePage {

    private final ObjectProvider<CasinoPage> casinoPageProvider;

    public MainPage(ObjectProvider<Page> page, LocalizationService ls,
                    ObjectProvider<CasinoPage> casinoPageProvider) {
        super(page, ls);
        this.casinoPageProvider = casinoPageProvider;
    }

    private static final int MOBILE_BREAKPOINT = 960;

    /**
     * Navigates to the casino page through the menu, adapting to screen size.
     */
    @SuppressWarnings("resource")
    public CasinoPage navigateToCasino() {
        int currentWidth = page().viewportSize().width;
        if (currentWidth < MOBILE_BREAKPOINT) {
            tabBar().clickCasino();
        } else {
            header().clickCasino();
        }
        page().waitForURL("**/casino");
        return casinoPageProvider.getObject();
    }

    /**
     * Verifies that the main page is loaded.
     *
     * @return current page object
     */
    public MainPage verifyIsLoaded() {
        header().verifyLogoVisible();
        verifyUrlContains("/");
        return this;
    }
}
