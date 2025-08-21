package com.example.testsupport.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.routing.PagePath;

@PagePath("/")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MainPage {
    private final Page page;
    private final LocalizationService loc;

    public MainPage(Page page, LocalizationService loc) {
        this.page = page;
        this.loc = loc;
    }

    /**
     * Возвращает локатор ссылки «Казино» в меню.
     *
     * @return локатор ссылки
     */
    public Locator casinoLink() {
        String casinoText = loc.get("header.menu.casino");
        return page.getByRole(AriaRole.NAVIGATION)
                .getByRole(AriaRole.LINK,
                        new Locator.GetByRoleOptions()
                                .setName(casinoText)
                                .setExact(true));
    }

    /**
     * Переходит на страницу казино через меню.
     *
     * @return объект {@link CasinoPage} после навигации
     */
    public CasinoPage clickCasino() {
        page.waitForNavigation(() -> casinoLink().click());
        return new CasinoPage(page);
    }

    /**
     * Проверяет, что текущий URL содержит ожидаемый путь.
     *
     * @param expectedPath ожидаемая подстрока URL
     * @return текущий объект {@link MainPage}
     */
    public MainPage verifyUrlContains(String expectedPath) {
        Assertions.assertThat(page.url()).contains(expectedPath);
        return this;
    }

    /**
     * Проверяет, что текущий URL равен ожидаемому URL.
     *
     * @param expectedUrl полный ожидаемый URL
     * @return текущий объект {@link MainPage}
     */
    public MainPage verifyUrlIs(String expectedUrl) {
        page.waitForURL(expectedUrl);
        Assertions.assertThat(page.url()).isEqualTo(expectedUrl);
        return this;
    }
}
