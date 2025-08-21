package com.example.testsupport.framework.routing;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Сервис для построения URL-адресов страниц с учетом текущего языка.
 * Является единым источником правды для генерации URL.
 */
@Component
public class UrlBuilder {
    private final AppProperties props;
    private final LocalizationService ls;

    public UrlBuilder(AppProperties props, LocalizationService ls) {
        this.props = props;
        this.ls = ls;
    }

    /**
     * Возвращает базовый URL для текущего языка (с префиксом или без).
     * @return Например, "https://spelet.lv" или "https://spelet.lv/ru"
     */
    public String getBaseUrl() {
        String currentLang = ls.getCurrentLangCode();
        String defaultLang = props.getDefaultLanguage();

        if (currentLang != null && currentLang.equalsIgnoreCase(defaultLang)) {
            return props.getBaseUrl();
        }
        return props.getBaseUrl() + "/" + currentLang;
    }

    /**
     * Возвращает полный URL для класса страницы, помеченного аннотацией {@link PagePath},
     * дополняя его указанными query-параметрами.
     *
     * @param pageClass   класс Page Object (например, {@code CasinoPage.class})
     * @param queryParams карта query-параметров. Может быть {@code null} или пустой.
     * @return полный URL для этой страницы с учетом языка и переданных параметров
     */
    public String getPageUrl(Class<?> pageClass, Map<String, String> queryParams) {
        PagePath pathAnnotation = pageClass.getAnnotation(PagePath.class);
        if (pathAnnotation == null) {
            throw new IllegalArgumentException(
                "Класс " + pageClass.getSimpleName() + " не имеет аннотации @PagePath");
        }
        String path = Objects.requireNonNull(pathAnnotation.value(),
                "Path in @PagePath cannot be null");
        String url = getBaseUrl() + path;

        Map<String, String> params = queryParams == null ? Collections.emptyMap() : queryParams;
        if (params.isEmpty()) {
            return url;
        }
        String query = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
        return url + "?" + query;
    }

    /**
     * Возвращает URL для указанного класса без дополнительных параметров.
     */
    public String getPageUrl(Class<?> pageClass) {
        return getPageUrl(pageClass, Collections.emptyMap());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
