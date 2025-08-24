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
 * Client for connecting to BrowserStack via the WebSocket API.
 */
@Component
public class BrowserStackClient {
    private final String username = System.getenv("BROWSERSTACK_USERNAME");
    private final String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

    /**
     * Creates a browser connected to BrowserStack.
     *
     * @param playwright Playwright instance
     * @return connected browser
     */
    public Browser connectBrowser(Playwright playwright) {
        ensureCreds();

        JSONObject caps = new JSONObject();
        caps.put("browserstack.username", username);
        caps.put("browserstack.accessKey", accessKey);
        caps.put("project", System.getProperty("bs.project", "Spelet LV"));
        caps.put("build", System.getProperty("bs.build",
                System.getenv().getOrDefault("BS_BUILD_NAME",
                        "spelet-lv-" + ZonedDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")))));
        caps.put("name", System.getProperty("bs.name", "Spelet test"));
        // Версии клиента/сервера Playwright
        caps.put("browserstack.playwrightVersion", "1.latest");
        caps.put("client.playwrightVersion", "1.48.0");

        // Диагностика
        caps.put("browserstack.console", System.getProperty("bs.console", "info"));
        caps.put("browserstack.networkLogs", System.getProperty("bs.networkLogs", "true"));
        caps.put("browserstack.video", System.getProperty("bs.video", "true"));
        caps.put("browserstack.debug", System.getProperty("bs.debug", "true"));
        caps.put("browserstack.maskCommands", "setValues, getValues, setCookies, getCookies");

        // (опционально) локальный туннель
        if ("true".equalsIgnoreCase(System.getProperty("bs.local"))) {
            caps.put("browserstack.local", "true");
            String localId = System.getProperty("bs.localIdentifier");
            if (localId != null && !localId.isBlank()) {
                caps.put("browserstack.localIdentifier", localId);
            }
        }
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
