package com.example.testsupport.pages;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.framework.utils.Breakpoints;
import com.example.testsupport.pages.components.FilterDrawerComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
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

    public CasinoPage(ObjectProvider<Page> page, AppProperties props, LocalizationService ls) {
        super(page, ls);
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
            return new FilterDrawerComponent(drawer, ls);
        });
    }
}
