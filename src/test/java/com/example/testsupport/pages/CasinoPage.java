package com.example.testsupport.pages;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Page;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Page object for the casino page.
 * Knows its own URL depending on current language.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CasinoPage extends BasePage {
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
     *
     * @return current page object
     */
    public CasinoPage verifyUrl() {
        verifyUrlContains(getExpectedPath());
        return this;
    }

    /**
     * Проверяет, что страница казино загружена и готова к работе.
     *
     * @return текущий объект страницы
     */
    public CasinoPage verifyIsLoaded() {
        verifyUrl();
        return this;
    }
}
