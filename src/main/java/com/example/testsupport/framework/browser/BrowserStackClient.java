package com.example.testsupport.framework.browser;

import com.example.testsupport.config.EnvironmentConfig;
import com.microsoft.playwright.*;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * Client for connecting to BrowserStack via the WebSocket API.
 */
@Component
public class BrowserStackClient {
    private final String username = System.getenv("BROWSERSTACK_USERNAME");
    private final String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private final EnvironmentConfig config;

    public BrowserStackClient(EnvironmentConfig config) {
        this.config = config;
    }

    /**
     * Creates a browser connected to BrowserStack.
     *
     * @param playwright Playwright instance
     * @return connected browser
     */
    public Browser connectBrowser(Playwright playwright) {
        ensureCreds();

        EnvironmentConfig.BrowserStackConfig bsProps = config.getBrowserstack();
        JSONObject caps = new JSONObject();
        caps.put("browserstack.username", username);
        caps.put("browserstack.accessKey", accessKey);
        caps.put("project", bsProps.getProject());
        caps.put("build", bsProps.getBuild());
        caps.put("name", bsProps.getName());
        caps.put("browserstack.playwrightVersion", "1.latest");
        caps.put("browserstack.console", "errors");
        caps.put("browserstack.networkLogs", "true");
        caps.put("browserstack.debug", "true");
        String deviceName = bsProps.getDeviceName();
        if (deviceName != null && !deviceName.isEmpty()) {
            buildMobileCapabilities(caps, bsProps);
        } else {
            buildDesktopCapabilities(caps, bsProps);
        }

        String browser = bsProps.getBrowser();
        return connect(playwright, browser, caps);
    }

    private void buildDesktopCapabilities(JSONObject caps, EnvironmentConfig.BrowserStackConfig bsProps) {
        caps.put("os", bsProps.getOs());
        caps.put("osVersion", bsProps.getOsVersion());
        caps.put("browserName", bsProps.getBrowser());
        caps.put("browserVersion", bsProps.getBrowserVersion());
    }

    private void buildMobileCapabilities(JSONObject caps, EnvironmentConfig.BrowserStackConfig bsProps) {
        caps.put("deviceName", bsProps.getDeviceName());
        if (bsProps.getOsVersion() != null) {
            caps.put("osVersion", bsProps.getOsVersion());
        }
        caps.put("browserName", bsProps.getBrowser());
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
