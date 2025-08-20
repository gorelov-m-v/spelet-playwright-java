package com.example.testsupport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String baseUrl = "https://spelet.lv";
    private String remote = "local";     // local | browserstack
    private String browser = "chromium"; // chromium | firefox | webkit
    private boolean headless = false;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getRemote() { return remote; }
    public void setRemote(String remote) { this.remote = remote; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public boolean isHeadless() { return headless; }
    public void setHeadless(boolean headless) { this.headless = headless; }
}
