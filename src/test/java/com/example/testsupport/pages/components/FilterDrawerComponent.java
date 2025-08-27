package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Component representing the filter drawer on the casino page.
 */
public class FilterDrawerComponent extends BaseComponent {

    private final LocalizationService ls;

    public FilterDrawerComponent(Locator root, LocalizationService ls) {
        super(root);
        this.ls = ls;
    }

    /**
     * Verifies that the filter drawer is loaded by checking its translated title.
     *
     * @return current component instance
     */
    public FilterDrawerComponent verifyIsLoaded() {
        return step("Проверка загрузки дровера фильтров", () -> {
            String title = ls.get("casino.filters.title");
            assertThat(root.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions()
                    .setName(title)
                    .setExact(true))).isVisible();
            return this;
        });
    }
}

