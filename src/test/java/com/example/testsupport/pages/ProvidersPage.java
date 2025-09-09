package com.example.testsupport.pages;

import com.example.testsupport.config.EnvironmentConfig;
import com.example.testsupport.framework.localization.LocalizationService;
import com.example.testsupport.pages.components.CookieBannerComponent;
import com.example.testsupport.pages.components.HeaderComponent;
import com.example.testsupport.pages.components.TabBarComponent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.testsupport.framework.utils.AllureHelper.step;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProvidersPage extends BasePage<ProvidersPage> {

    private final Locator providerGrid;

    public ProvidersPage(Page page,
                         LocalizationService ls,
                         EnvironmentConfig config,
                         ObjectProvider<HeaderComponent> headerProvider,
                         ObjectProvider<TabBarComponent> tabBarProvider,
                         ObjectProvider<CookieBannerComponent> cookieBannerProvider) {
        super(page, ls, config, headerProvider, tabBarProvider, cookieBannerProvider);
        this.providerGrid = page.locator("div[role='grid']");
    }

    @Override
    protected String getExpectedPath() {
        return getLocalizedPath("/casino/providers");
    }

    @Override
    public ProvidersPage verifyUrl() {
        return step("[ProvidersPage] Проверка URL страницы 'Провайдеры'", () -> {
            verifyUrlContains(getExpectedPath());
            return this;
        });
    }

    @Override
    public ProvidersPage verifyIsLoaded() {
        return step("[ProvidersPage] Проверка загрузки страницы 'Провайдеры'", () -> {
            assertThat(providerGrid).isVisible();
            verifyUrl();
            return this;
        });
    }

    public List<String> getProviderNames() {
        return step("[ProvidersPage] Получение списка провайдеров", () ->
                providerGrid.locator("[role='row']").all().stream()
                        .map(row -> row.getAttribute("aria-label"))
                        .toList()
        );
    }
}

