package com.example.testsupport.framework.browser;

import com.microsoft.playwright.*;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Клиент для подключения к BrowserStack через WebSocket API.
 */
@Component
public class BrowserStackClient {
    private final String username = System.getenv("BROWSERSTACK_USERNAME");
    private final String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

    /**
     * Создает подключенный к BrowserStack браузер.
     *
     * @param playwright экземпляр Playwright
     * @return подключенный браузер
     */
    public Browser connectBrowser(Playwright playwright) {
        ensureCreds();

        JSONObject caps = new JSONObject();
        caps.put("browserstack.username", username);
        caps.put("browserstack.accessKey", accessKey);
        caps.put("project", "Spelet LV");
        caps.put("build", "spelet-lv-" + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")));
        caps.put("name", System.getProperty("bs.name", "Spelet test"));
        caps.put("browserstack.playwrightVersion", "1.latest");
        caps.put("browserstack.console", "errors");
        caps.put("browserstack.networkLogs", "true");
        caps.put("browserstack.debug", "true");
        String deviceName = System.getProperty("bs.deviceName");
        if (deviceName != null && !deviceName.isEmpty()) {
            buildMobileCapabilities(caps);
        } else {
            buildDesktopCapabilities(caps);
        }

        String browser = System.getProperty("bs.browser", "chrome");
        return connect(playwright, browser, caps);
    }

    private void buildDesktopCapabilities(JSONObject caps) {
        String browser = System.getProperty("bs.browser", "chrome");
        String os = System.getProperty("bs.os", "Windows");
        String osVer = System.getProperty("bs.osVersion", "10");
        String browserVersion = System.getProperty("bs.browserVersion", "latest");

        caps.put("os", os);
        caps.put("osVersion", osVer);
        caps.put("browserName", browser);
        caps.put("browserVersion", browserVersion);
    }

    private void buildMobileCapabilities(JSONObject caps) {
        String browser = System.getProperty("bs.browser", "chrome");
        String deviceName = System.getProperty("bs.deviceName");
        String osVersion = System.getProperty("bs.osVersion");

        caps.put("deviceName", deviceName);
        if (osVersion != null) {
            caps.put("osVersion", osVersion);
        }
        caps.put("browserName", browser);
    }

    private Browser connect(Playwright playwright, String browser, JSONObject caps) {
        String ws = "wss://cdp.browserstack.com/playwright?caps=" + urlEncode(caps.toString());

        BrowserType type = switch (browser.toLowerCase()) {
            case "firefox", "playwright-firefox" -> playwright.firefox();
            case "safari", "playwright-webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };
        return type.connect(ws);
    }

    private void ensureCreds() {
        if (Objects.isNull(username) || Objects.isNull(accessKey) || username.isEmpty() || accessKey.isEmpty()) {
            throw new IllegalStateException("BROWSERSTACK_USERNAME / BROWSERSTACK_ACCESS_KEY are not set");
        }
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
