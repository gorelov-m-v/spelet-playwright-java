package com.example.testsupport.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.example.testsupport.framework.localization.LocalizationService;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MainPage extends BasePage {

    public MainPage(Page page, LocalizationService ls) {
        super(page, ls);
    }

    /**
     * Возвращает локатор ссылки «Казино» в меню.
     *
     * @return локатор ссылки
     */
    public Locator casinoLink() {
        String casinoText = ls.get("header.menu.casino");
        return page.getByRole(AriaRole.NAVIGATION)
                .getByRole(AriaRole.LINK,
                        new Locator.GetByRoleOptions()
                                .setName(casinoText)
                                .setExact(true));
    }

    /**
     * Переходит на страницу казино через меню.
     */
    public void clickCasino() {
        page.waitForNavigation(() -> casinoLink().click());
    }
}
