package main.config;

import main.framework.browser.BrowserName;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
/**
 * Application settings read from configuration files.
 */
public class AppProperties {
    private String baseUrl = "https://spelet.lv";
    private BrowserName browser = BrowserName.CHROMIUM;
    private boolean headless = false;
    private String language = "lv";
    private String defaultLanguage = "lv";
    private List<String> languages;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public BrowserName getBrowser() { return browser; }
    public void setBrowser(BrowserName browser) { this.browser = browser; }

    public boolean isHeadless() { return headless; }
    public void setHeadless(boolean headless) { this.headless = headless; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDefaultLanguage() { return defaultLanguage; }
    public void setDefaultLanguage(String defaultLanguage) { this.defaultLanguage = defaultLanguage; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }
}
