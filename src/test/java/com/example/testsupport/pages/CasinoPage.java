package com.example.testsupport.pages;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.utils.Breakpoints;
import com.example.testsupport.pages.components.FilterDrawerComponent;
import com.example.testsupport.pages.components.AuthModalComponent;
import com.example.testsupport.pages.components.HeaderComponent;
import com.example.testsupport.pages.components.TabBarComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Page object for the casino page.
 * Knows its own URL depending on current language.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CasinoPage extends BasePage<CasinoPage> {
    private final AppProperties props;

    public CasinoPage(ObjectProvider<Page> page, AppProperties props, LocalizationService ls,
                      ObjectProvider<HeaderComponent> headerProvider,
                      ObjectProvider<TabBarComponent> tabBarProvider) {
        super(page, ls, headerProvider, tabBarProvider);
        this.props = props;
    }

    /**
     * Returns the expected relative path for the casino page based on language.
     *
     * @return expected path like "/casino" or "/ru/casino"
     */
    public String getExpectedPath() {
        String lang = ls.getCurrentLangCode();
        if (lang == null || lang.equals(props.getDefaultLanguage())) {
            return "/casino";
        }
        return "/" + lang + "/casino";
    }

    /**
     * Verifies that current URL contains expected path.
     */
    public CasinoPage verifyUrl() {
        return step("Проверка URL страницы 'Казино'", () -> {
            verifyUrlContains(getExpectedPath());
            return this;
        });
    }

    /**
     * Verifies that the casino page is loaded.
     *
     * @return current page object
     */
    @Override
    public CasinoPage verifyIsLoaded() {
        return step("Проверка загрузки страницы 'Казино'", () -> {
            header().verifyLogoVisible();
            verifyUrlContains(getExpectedPath());
            return this;
        });
    }

    /**
     * Opens the filter drawer by clicking the corresponding button.
     *
     * @return filter drawer component
     */
    public FilterDrawerComponent openFilters() {
        return step("Открытие панели фильтров", () -> {
            Locator button;
            int width = page().viewportSize() != null ? page().viewportSize().width : Integer.MAX_VALUE;
            if (width < Breakpoints.TABLET) {
                button = page().locator("div.d_block.pos_relative.w768\\:d_none > button");
            } else {
                String buttonText = ls.get("casino.filters.button");
                button = page().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions()
                        .setName(buttonText)
                        .setExact(true));
            }
            button.click();
            Locator drawer = page().locator("div.drawer__headerWrapper").locator("..");
            return new FilterDrawerComponent(drawer, ls, this);
        });
    }
    /**
     * Types the given query into the casino search field.
     *
     * @param query game name or part of it
     * @return current page object
     */
    public CasinoPage typeInSearch(String query) {
        return step(String.format("Вводим в поле поиска '%s'", query), () -> {
            String searchLabel = ls.get("casino.search.input");
            page().getByRole(AriaRole.SEARCHBOX, new Page.GetByRoleOptions()
                    .setName(searchLabel)
                    .setExact(true))
                .fill(query);
            return this;
        });
    }

    /**
     * Waits for a game card with the specified name to become visible.
     *
     * @param gameName expected game title
     * @return current page object
     */
    public CasinoPage waitForGameVisible(String gameName) {
        return step(String.format("Ожидаем отображения игры '%s'", gameName), () -> {
            Locator card = page().locator(".GameCard__root").filter(new Locator.FilterOptions()
                    .setHasText(gameName));
            assertThat(card.first()).isVisible();
            return this;
        });
    }

    /**
     * Clicks the play button for the specified game and returns the auth prompt modal.
     *
     * @param gameName name of the game whose play button should be clicked
     * @return authorization modal component
     */
    public AuthModalComponent clickPlay(String gameName) {
        return step(String.format("Запускаем игру '%s'", gameName), () -> {
            Locator card = page().locator(".GameCard__root").filter(new Locator.FilterOptions()
                    .setHasText(gameName)).first();
            card.getByRole(AriaRole.BUTTON).click();
            String promptTitle = ls.get("casino.play.prompt");
            Locator dialog = page().getByText(promptTitle, new Page.GetByTextOptions()
                    .setExact(true)).locator("..");
            return new AuthModalComponent(dialog, ls);
        });
    }
}
