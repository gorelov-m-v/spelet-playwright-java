package com.example.testsupport.pages;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Page object for the casino page.
 * Knows its own URL depending on current language.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CasinoPage {
    private final Page page;
    private final AppProperties props;
    private final LocalizationService ls;

    public CasinoPage(Page page, AppProperties props, LocalizationService ls) {
        this.page = page;
        this.props = props;
        this.ls = ls;
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
        String expected = getExpectedPath();
        String current = page.url();
        Assertions.assertTrue(
                current.contains(expected),
                String.format("Ожидалось, что URL содержит '%s', но фактический URL '%s'", expected, current)
        );
        return this;
    }
}
