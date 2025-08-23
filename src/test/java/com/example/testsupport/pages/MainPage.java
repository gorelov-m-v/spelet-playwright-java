package com.example.testsupport.pages;

import com.microsoft.playwright.Page;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.example.testsupport.framework.localization.LocalizationService;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MainPage extends BasePage {

    public MainPage(ObjectProvider<Page> page, LocalizationService ls) {
        super(page, ls);
    }

    private static final int MOBILE_BREAKPOINT = 960;

    /**
     * Переходит на страницу казино через меню, адаптируясь под размер экрана.
     */
    public void navigateToCasino() {
        int currentWidth = page().viewportSize().width;
        if (currentWidth < MOBILE_BREAKPOINT) {
            page().waitForNavigation(() -> tabBar().clickCasino());
        } else {
            page().waitForNavigation(() -> header().clickCasino());
        }
    }
}
