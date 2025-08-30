package com.example.testsupport.pages;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.utils.Breakpoints;
import com.example.testsupport.pages.components.FilterDrawerComponent;
import com.example.testsupport.pages.components.AuthModalComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.example.testsupport.framework.utils.AllureHelper.step;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the casino page.
 * Knows its own URL depending on current language.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CasinoPage extends BasePage<CasinoPage> {

    private final Locator mobileFilterButton;
    private final Locator desktopFilterButton;
    private final Locator filterDrawerRoot;
    private final Locator searchInput;
    private final Locator gameCards;
    private final Locator authDialog;

    public CasinoPage(ObjectProvider<Page> page, AppProperties props, LocalizationService ls) {
        super(page, ls, props);
        this.mobileFilterButton = page().locator("div.d_block.pos_relative.w768\\:d_none > button");
        String buttonText = ls.get("casino.filters.button");
        this.desktopFilterButton = page().getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(buttonText).setExact(true));
        this.filterDrawerRoot = page().locator("div.drawer__headerWrapper").locator("..");
        String searchLabel = ls.get("casino.search.input");
        this.searchInput = page().getByRole(AriaRole.SEARCHBOX, new Page.GetByRoleOptions().setName(searchLabel).setExact(true));
        this.gameCards = page().locator(".GameCard__root");
        String promptTitle = ls.get("casino.play.prompt");
        this.authDialog = page().getByText(promptTitle, new Page.GetByTextOptions().setExact(true)).locator("..");
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
                button = mobileFilterButton;
            } else {
                button = desktopFilterButton;
            }
            button.click();
            return new FilterDrawerComponent(filterDrawerRoot, ls, this);
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
            searchInput.fill(query);
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
            Locator card = gameCards.filter(new Locator.FilterOptions().setHasText(gameName));
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
            Locator card = gameCards.filter(new Locator.FilterOptions().setHasText(gameName)).first();
            card.getByRole(AriaRole.BUTTON).click();
            return new AuthModalComponent(authDialog, ls);
        });
    }
}

