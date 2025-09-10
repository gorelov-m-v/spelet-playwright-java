package com.example.testsupport.pages;

import com.example.testsupport.config.EnvironmentConfig;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.utils.Breakpoints;
import com.example.testsupport.pages.components.FilterDrawerComponent;
import com.example.testsupport.pages.components.AuthModalComponent;
import com.example.testsupport.pages.components.HeaderComponent;
import com.example.testsupport.pages.components.TabBarComponent;
import com.example.testsupport.pages.components.CookieBannerComponent;
import com.example.testsupport.pages.components.CategoryTabsComponent;
import com.example.testsupport.pages.components.NavigationPanelComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.List;

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
    private final Locator lobbyButton;
    private final Locator searchInput;
    private final Locator gameCards;
    private final ObjectProvider<FilterDrawerComponent> filterDrawerComponentProvider;
    private final ObjectProvider<AuthModalComponent> authModalComponentProvider;
    private final ObjectProvider<CategoryTabsComponent> categoryTabsComponentProvider;
    private final ObjectProvider<NavigationPanelComponent> navigationPanelComponentProvider;

    public CasinoPage(Page page,
                      EnvironmentConfig config,
                      LocalizationService ls,
                      ObjectProvider<FilterDrawerComponent> filterDrawerComponentProvider,
                      ObjectProvider<AuthModalComponent> authModalComponentProvider,
                      ObjectProvider<HeaderComponent> headerProvider,
                      ObjectProvider<TabBarComponent> tabBarProvider,
                      ObjectProvider<CookieBannerComponent> cookieBannerProvider,
                      ObjectProvider<CategoryTabsComponent> categoryTabsComponentProvider,
                      ObjectProvider<NavigationPanelComponent> navigationPanelComponentProvider) {
        super(page, ls, config, headerProvider, tabBarProvider, cookieBannerProvider);
        this.filterDrawerComponentProvider = filterDrawerComponentProvider;
        this.authModalComponentProvider = authModalComponentProvider;
        this.categoryTabsComponentProvider = categoryTabsComponentProvider;
        this.navigationPanelComponentProvider = navigationPanelComponentProvider;
        this.mobileFilterButton = page.locator("div.d_block.pos_relative.w768\\:d_none > button");
        String buttonText = ls.get("casino.filters.button");
        this.desktopFilterButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(buttonText).setExact(true));
        this.lobbyButton = page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Lobby").setExact(true));
        String searchLabel = ls.get("casino.search.input");
        this.searchInput = page.getByRole(AriaRole.SEARCHBOX, new Page.GetByRoleOptions().setName(searchLabel).setExact(true));
        this.gameCards = page.locator(".GameCard__root");
    }

    @Override
    protected String getExpectedPath() {
        return getLocalizedPath("/casino");
    }

    /**
     * Verifies that current URL contains expected path.
     */
    @Override
    public CasinoPage verifyUrl() {
        return step("[CasinoPage] Проверка URL страницы 'Казино'", () -> {
            verifyUrlContains(getExpectedPath());
            return this;
        });
    }

    /**
     * Verifies that the lobby button is visible.
     */
    public CasinoPage verifyLobbyButton() {
        return step("[CasinoPage] Проверка кнопки 'Лобби'", () -> {
            assertThat(lobbyButton).isVisible();
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
        return step("[CasinoPage] Проверка загрузки страницы 'Казино'", () -> {
            verifyLobbyButton();
            verifyUrl();
            return this;
        });
    }

    /**
     * Opens the filter drawer by clicking the corresponding button.
     *
     * @return filter drawer component
     */
    public FilterDrawerComponent openFilters() {
        return step("[CasinoPage] Открытие панели фильтров", () -> {
            Locator button;
            int width = page.viewportSize() != null ? page.viewportSize().width : Integer.MAX_VALUE;
            if (width < Breakpoints.TABLET) {
                button = mobileFilterButton;
            } else {
                button = desktopFilterButton;
            }
            button.click();
            return filterDrawerComponentProvider.getObject();
        });
    }
    /**
     * Provides access to the horizontal category tabs component.
     */
    public CategoryTabsComponent categoryTabs() {
        return categoryTabsComponentProvider.getObject();
    }

    /**
     * Provides access to the navigation panel component.
     */
    public NavigationPanelComponent navigationPanel() {
        return navigationPanelComponentProvider.getObject();
    }

    /**
     * Types the given query into the casino search field.
     *
     * @param query game name or part of it
     * @return current page object
     */
    public CasinoPage typeInSearch(String query) {
        return step(String.format("[CasinoPage] Вводим в поле поиска '%s'", query), () -> {
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
        return step(String.format("[CasinoPage] Ожидаем отображения игры '%s'", gameName), () -> {
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
        return step(String.format("[CasinoPage] Запускаем игру '%s'", gameName), () -> {
            Locator card = gameCards.filter(new Locator.FilterOptions().setHasText(gameName)).first();
            card.getByRole(AriaRole.BUTTON).click();
            return authModalComponentProvider.getObject();
        });
    }
}
