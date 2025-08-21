package com.example.testsupport.framework.routing;

import com.example.testsupport.config.AppProperties;
import com.example.testsupport.framework.localization.LocalizationService;
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
     * Возвращает полный URL для указанного относительного пути.
     * @param path Относительный путь, например, "/casino"
     * @return Полный URL, например, "https://spelet.lv/ru/casino"
     */
    public String getPageUrl(String path) {
        if (path == null || path.isEmpty()) {
            return getBaseUrl();
        }
        return getBaseUrl() + (path.startsWith("/") ? path : "/" + path);
    }
}
