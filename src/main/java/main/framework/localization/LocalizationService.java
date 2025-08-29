package main.framework.localization;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Provides UI element translations based on the current test language.
 * Locale data is stored in a {@link ThreadLocal} enabling parallel test execution.
 */
@Component
public class LocalizationService {
    private static final ThreadLocal<Properties> LOCALE = new ThreadLocal<>();
    private static final ThreadLocal<String> LANG_CODE = new ThreadLocal<>();

    /**
     * Loads locale properties for the given language code.
     *
     * @param languageCode e.g., "lv", "ru", "en"
     */
    public void loadLocale(String languageCode) {
        String resourcePath = "/locales/" + languageCode + ".properties";
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Locale file not found: " + resourcePath);
            }
            Properties props = new Properties();
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                props.load(reader);
            }
            LOCALE.set(props);
            LANG_CODE.set(languageCode);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load locale for " + languageCode, e);
        }
    }

    /**
     * Returns a translation for the specified key from the loaded locale.
     *
     * @param key translation key
     * @return localized value
     */
    public String get(String key) {
        Properties props = LOCALE.get();
        if (props == null) {
            throw new IllegalStateException("Locale not loaded");
        }
        return props.getProperty(key, key);
    }

    /**
     * @return current language code
     */
    public String getCurrentLangCode() {
        return LANG_CODE.get();
    }
}
