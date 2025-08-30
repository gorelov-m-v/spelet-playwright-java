package com.example.testsupport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Properties for BrowserStack configuration.
 */
@ConfigurationProperties(prefix = "bs")
public class BrowserStackProperties {
    private String os = "Windows";
    private String osVersion = "10";
    private String browser = "chrome";
    private String browserVersion = "latest";
    private String deviceName;
    private String project = "Spelet LV";
    private String build = "spelet-lv-" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
    private String name = "Spelet test";

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

