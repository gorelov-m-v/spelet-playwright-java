package com.example.testsupport.framework.browser;

import com.microsoft.playwright.*;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Client for connecting to BrowserStack via the WebSocket API.
 */
@Component
public class BrowserStackClient {
    private final BrowserStackSessionManager sessionManager;

    public BrowserStackClient(BrowserStackSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Creates a browser connected to BrowserStack.
     *
     * @param playwright Playwright instance
     * @return connected browser
     */
    public Browser connectBrowser(Playwright playwright, String testName) {
        JSONObject caps = new JSONObject();
        caps.put("name", testName);
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
        String wsUrl = "wss://cdp.browserstack.com/playwright?caps=" + urlEncode(caps.toString());

        BrowserType type = switch (browser.toLowerCase()) {
            case "firefox", "playwright-firefox" -> playwright.firefox();
            case "safari", "playwright-webkit" -> playwright.webkit();
            default -> playwright.chromium();
        };
        Browser browserInstance = type.connectOverCDP(wsUrl);

        try {
            JSONObject details = new JSONObject(browserInstance.version());
            if (details.has("browserstack")) {
                JSONObject bs = details.getJSONObject("browserstack");
                if (bs.has("sessionId")) {
                    sessionManager.setSessionId(bs.getString("sessionId"));
                }
                if (bs.has("dashboardUrl")) {
                    System.out.println("BrowserStack dashboard: " + bs.getString("dashboardUrl"));
                }
            }
        } catch (Exception ignored) {
            // ignore parsing issues
        }

        return browserInstance;
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
