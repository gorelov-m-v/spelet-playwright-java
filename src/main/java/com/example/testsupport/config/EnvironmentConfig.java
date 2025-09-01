package com.example.testsupport.config;

import com.example.testsupport.framework.browser.BrowserName;
import com.example.testsupport.framework.device.Device;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Root configuration object for test environments loaded from YAML profiles.
 */
@Data
@Component
@ConfigurationProperties(prefix = "env")
public class EnvironmentConfig {
    private ApiConfig api = new ApiConfig();
    private BrowserConfig browser = new BrowserConfig();
    private BrowserStackConfig browserstack = new BrowserStackConfig();
    private TestDevices testDevices = new TestDevices();

    @Data
    public static class ApiConfig {
        private String baseUrl = "https://spelet.lv";
    }

    @Data
    public static class BrowserConfig {
        private BrowserName name = BrowserName.CHROMIUM;
        private boolean headless = false;
        private String language = "lv";
        private String defaultLanguage = "lv";
        private List<String> languages;
    }

    @Data
    public static class BrowserStackConfig {
        private String os = "Windows";
        private String osVersion = "10";
        private String browser = "chrome";
        private String browserVersion = "latest";
        private String deviceName;
        private String project = "Spelet LV";
        private String build = "spelet-lv-" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        private String name = "Spelet test";
    }

    @Data
    public static class TestDevices {
        private List<Device> platforms;
    }
}
