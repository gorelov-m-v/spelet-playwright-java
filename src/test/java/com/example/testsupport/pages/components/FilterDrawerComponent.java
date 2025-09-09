package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.CasinoPage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Objects;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Component representing the filter drawer on the casino page.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FilterDrawerComponent extends BaseComponent {

    private final ObjectProvider<CasinoPage> casinoPageProvider;
    private final Locator title;
    private final Locator showButton;

    public FilterDrawerComponent(Page page,
                                 LocalizationService ls,
                                 ObjectProvider<CasinoPage> casinoPageProvider) {
        super(page.locator("div.drawer__headerWrapper").locator(".."));
        this.casinoPageProvider = casinoPageProvider;
        String titleText = ls.get("casino.filters.title");
        this.title = root().getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setName(titleText).setExact(true));
        String showText = ls.get("casino.filters.show");
        this.showButton = root().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(showText).setExact(true));
    }

    /**
     * Verifies that the filter drawer is loaded by checking its translated title.
     *
     * @return current component instance
     */
    public FilterDrawerComponent verifyIsLoaded() {
        return step("[FilterDrawerComponent] Проверка загрузки дровера фильтров", () -> {
            assertThat(title).isVisible();
            return this;
        });
    }

    /**
     * Returns the list of provider names displayed in the drawer.
     *
     * @return list of provider names
     */
    public List<String> getProviderNames() {
        return step("[FilterDrawerComponent] Получение списка провайдеров", () ->
                root().locator(".provider-tag-group__tag").all().stream()
                        .map(tag -> Objects.requireNonNull(tag.getAttribute("aria-label")))
                        .toList()
        );
    }

    /**
     * Returns the value of the provider count badge.
     *
     * @return number of providers shown in the badge
     */
    public int getProviderCount() {
        return step("[FilterDrawerComponent] Получение значения счётчика брендов", () ->
                Integer.parseInt(Objects.requireNonNull(
                        root().locator("div:has(> #brands-label) > .badge").textContent()).trim())
        );
    }

    /**
     * Returns the list of category names displayed in the drawer.
     *
     * @return list of category names
     */
    public List<String> getCategoryNames() {
        return step("[FilterDrawerComponent] Получение списка категорий", () ->
                root().locator("[aria-labelledby='categories-label']").locator(".tag-group__item").all().stream()
                        .map(tag -> Objects.requireNonNull(tag.getAttribute("aria-label")))
                        .toList()
        );
    }

    /**
     * Returns the list of collection names displayed in the drawer.
     *
     * @return list of collection names
     */
    public List<String> getCollectionNames() {
        return step("[FilterDrawerComponent] Получение списка коллекций", () ->
                root().locator("[aria-labelledby='collections-label']").locator(".tag-group__item").all().stream()
                        .map(tag -> Objects.requireNonNull(tag.getAttribute("aria-label")))
                        .toList()
        );
    }

    /**
     * Selects a provider within the drawer by its visible name.
     *
     * @param providerName provider label as displayed in UI
     * @return current component instance
     */
    public FilterDrawerComponent selectProvider(String providerName) {
        return step(String.format("[FilterDrawerComponent] Выбор провайдера '%s'", providerName), () -> {
            root().getByRole(AriaRole.ROW, new Locator.GetByRoleOptions()
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
        return step("[FilterDrawerComponent] Применение фильтров", () -> {
            showButton.click();
            return casinoPageProvider.getObject();
        });
    }
}

