package com.example.testsupport.pages.components;

import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.ProvidersPage;
import com.example.testsupport.framework.utils.Breakpoints;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Locator;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.example.testsupport.framework.utils.AllureHelper.step;

/**
 * Component for the navigation panel on the casino page.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NavigationPanelComponent extends BaseComponent {

    private final LocalizationService ls;
    private final ObjectProvider<ProvidersPage> providersPageProvider;
    private final Page page;
    private final Locator desktopProvidersTab;
    private final Locator mobileProvidersTab;

    public NavigationPanelComponent(Page page, LocalizationService ls, ObjectProvider<ProvidersPage> providersPageProvider) {
        super(page.locator("div.d_flex.gap_1.ov-x_auto.pos_relative").first());
        this.page = page;
        this.ls = ls;
        this.providersPageProvider = providersPageProvider;
        String providers = ls.get("casino.navigation.providers");
        this.desktopProvidersTab = root().locator("div.navigationTab__root--size_md")
                .filter(new Locator.FilterOptions().setHasText(providers));
        this.mobileProvidersTab = root().locator("div.navigationTab__root--size_sm")
                .filter(new Locator.FilterOptions().setHasText(providers));
    }

    /**
     * Returns a list of navigation panel category titles displayed on the page.
     *
     * @return list of navigation category titles in display order
     */
    public List<String> getTitles() {
        return step("[NavigationPanelComponent] Получение категорий навигационной панели", () -> {
            List<String> titles = root().locator("span.navigationTab__text").allInnerTexts();
            String providers = ls.get("casino.navigation.providers");
            return titles.stream()
                    .filter(title -> !title.equals(providers))
                    .toList();
        });
    }

    /**
     * Clicks the providers tab and opens providers page.
     */
    public ProvidersPage clickProviders() {
        return step("[NavigationPanelComponent] Переход на страницу 'Провайдеры'", () -> {
            int width = page.viewportSize() != null ? page.viewportSize().width : Integer.MAX_VALUE;
            Locator tab = width < Breakpoints.TABLET ? mobileProvidersTab : desktopProvidersTab;
            tab.click();
            return providersPageProvider.getObject().verifyIsLoaded();
        });
    }
}
