package com.example.testsupport.framework.routing;

import com.microsoft.playwright.Page;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
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

    /** Проверяет наличие ключа query-параметра. */
    public void urlContainsKey(String key) {
        Assertions.assertThat(getQueryParams()).containsKey(key);
    }

    /** Проверяет наличие пары ключ-значение среди query-параметров. */
    public void urlContainsQueryParam(String key, String value) {
        Assertions.assertThat(getQueryParams()).containsEntry(key, value);
    }

    /** Проверяет, что URL содержит все указанные query-параметры. */
    public void urlContainsQueryParams(Map<String, String> params) {
        Assertions.assertThat(getQueryParams()).containsAllEntriesOf(params);
    }

    private Map<String, String> getQueryParams() {
        String query = URI.create(page.url()).getRawQuery();
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.split("&"))
                .map(p -> p.split("=", 2))
                .collect(Collectors.toMap(
                        p -> decode(p[0]),
                        p -> p.length > 1 ? decode(p[1]) : ""));
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
