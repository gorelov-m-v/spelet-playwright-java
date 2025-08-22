package com.example.testsupport.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
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
     * Возвращает локатор элемента «Казино» в меню, учитывая адаптивную верстку.
     *
     * @return локатор ссылки или вкладки «Казино»
     */
    public Locator casinoLink() {
        String casinoText = ls.get("header.menu.casino");
        int currentWidth = page().viewportSize().width;

        if (currentWidth < MOBILE_BREAKPOINT) {
            // --- Локатор для мобильной версии ---
            return page().getByRole(
                    AriaRole.TAB,
                    new Page.GetByRoleOptions().setName(casinoText).setExact(true)
            );
        } else {
            // --- Локатор для десктопной версии ---
            return page().getByRole(AriaRole.NAVIGATION)
                    .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions()
                            .setName(casinoText)
                            .setExact(true));
        }
    }

    /**
     * Переходит на страницу казино через меню.
     */
    public void clickCasino() {
        page().waitForNavigation(() -> casinoLink().click());
    }
}
