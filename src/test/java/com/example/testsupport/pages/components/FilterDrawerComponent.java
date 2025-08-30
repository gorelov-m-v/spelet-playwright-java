package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.CasinoPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Component representing the filter drawer on the casino page.
 */
public class FilterDrawerComponent extends BaseComponent {

    private final LocalizationService ls;
    private final CasinoPage casinoPage;
    private final Locator title;
    private final Locator showButton;

    public FilterDrawerComponent(Locator root, LocalizationService ls, CasinoPage casinoPage) {
        super(root);
        this.ls = ls;
        this.casinoPage = casinoPage;
        String titleText = ls.get("casino.filters.title");
        this.title = root.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setName(titleText).setExact(true));
        String showText = ls.get("casino.filters.show");
        this.showButton = root.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(showText).setExact(true));
    }

    /**
     * Verifies that the filter drawer is loaded by checking its translated title.
     *
     * @return current component instance
     */
    public FilterDrawerComponent verifyIsLoaded() {
        return step("Проверка загрузки дровера фильтров", () -> {
            assertThat(title).isVisible();
            return this;
        });
    }

    /**
     * Selects a provider within the drawer by its visible name.
     *
     * @param providerName provider label as displayed in UI
     * @return current component instance
     */
    public FilterDrawerComponent selectProvider(String providerName) {
        return step(String.format("Выбор провайдера '%s'", providerName), () -> {
            root.getByRole(AriaRole.ROW, new Locator.GetByRoleOptions()
                    .setName(providerName)
                    .setExact(true))
                .click();
            return this;
        });
    }

    /**
     * Applies the selected filters by clicking the translated "Show" button.
     */
    public CasinoPage clickShow() {
        return step("Применение фильтров", () -> {
            showButton.click();
            return casinoPage;
        });
    }
}

