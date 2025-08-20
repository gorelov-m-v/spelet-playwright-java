package com.example.testsupport.framework.config;

import java.util.List;

/**
 * POJO для десериализации JSON-конфигурации окружений.
 */
public class EnvironmentConfig {
    private String baseUrl;
    private List<String> languages;
    private String defaultLanguage;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public String getDefaultLanguage() { return defaultLanguage; }
    public void setDefaultLanguage(String defaultLanguage) { this.defaultLanguage = defaultLanguage; }
}
