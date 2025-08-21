package com.example.testsupport.framework.routing;

import com.microsoft.playwright.Page;
import org.assertj.core.api.Assertions;
import org.springframework.stereotype.Component;

/**
 * Сервис для проверки, что браузер находится на ожидаемой странице.
 */
@Component
public class PageAsserter {
    private final Page page;
    private final UrlBuilder urlBuilder;

    public PageAsserter(Page page, UrlBuilder urlBuilder) {
        this.page = page;
        this.urlBuilder = urlBuilder;
    }

    /**
     * Проверяет, что текущий URL соответствует URL указанного Page Object.
     *
     * @param pageClass класс PO, помеченный {@link PagePath}
     */
    public void amOn(Class<?> pageClass) {
        String expectedUrl = urlBuilder.getPageUrl(pageClass);
        page.waitForURL(url -> url.startsWith(expectedUrl));
        Assertions.assertThat(page.url())
                .withFailMessage("Ожидали быть на странице %s (%s), но оказались на %s",
                        pageClass.getSimpleName(), expectedUrl, page.url())
                .startsWith(expectedUrl);
    }
}
