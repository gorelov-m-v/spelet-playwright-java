package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Component representing the filters drawer.
 */
public class FiltersDrawerComponent extends BaseComponent {
    private final LocalizationService ls;

    public FiltersDrawerComponent(Locator root, LocalizationService ls) {
        super(root);
        this.ls = ls;
    }

    /**
     * Verifies that the filters drawer is visible by checking its header.
     *
     * @return current component instance
     */
    public FiltersDrawerComponent verifyIsVisible() {
        return step("Проверка, что дровер фильтров отображается", () -> {
            String title = ls.get("casino.filters.drawer.title");
            assertThat(root.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions()
                    .setName(title)
                    .setExact(true))).isVisible();
            return this;
        });
    }
}

