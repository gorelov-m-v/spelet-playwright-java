package com.example.testsupport.localization;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Предоставляет переводы элементов UI на основе текущего языка теста.
 * Данные локали хранятся в {@link ThreadLocal}, что позволяет запускать тесты параллельно.
 */
@Component
public class LocalizationService {
    private static final ThreadLocal<Properties> LOCALE = new ThreadLocal<>();
    private static final ThreadLocal<String> LANG_CODE = new ThreadLocal<>();

    /**
     * Загружает свойства локали для заданного кода языка.
     *
     * @param languageCode например, "lv", "ru", "en"
     */
    public void loadLocale(String languageCode) {
        String resourcePath = "/locales/" + languageCode + ".properties";
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Locale file not found: " + resourcePath);
            }
            Properties props = new Properties();
            props.load(in);
            LOCALE.set(props);
            LANG_CODE.set(languageCode);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load locale for " + languageCode, e);
        }
    }

    /**
     * Возвращает перевод для указанного ключа из загруженной локали.
     *
     * @param key ключ перевода
     * @return локализованное значение
     */
    public String get(String key) {
        Properties props = LOCALE.get();
        if (props == null) {
            throw new IllegalStateException("Locale not loaded");
        }
        return props.getProperty(key, key);
    }

    /**
     * @return код текущего языка
     */
    public String getCurrentLangCode() {
        return LANG_CODE.get();
    }
}
