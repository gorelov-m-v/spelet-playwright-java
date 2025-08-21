package com.example.testsupport.framework.routing;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
     * Возвращает полный URL для класса страницы, помеченного аннотацией {@link PagePath}.
     *
     * @param pageClass класс Page Object (например, {@code CasinoPage.class})
     * @return полный URL для этой страницы с учетом языка
     */
    public String getPageUrl(Class<?> pageClass) {
        PagePath pathAnnotation = pageClass.getAnnotation(PagePath.class);
        if (pathAnnotation == null) {
            throw new IllegalArgumentException(
                "Класс " + pageClass.getSimpleName() + " не имеет аннотации @PagePath");
        }
        String path = Objects.requireNonNull(pathAnnotation.value(),
                "Path in @PagePath cannot be null");
        return getBaseUrl() + path;
    }
}
