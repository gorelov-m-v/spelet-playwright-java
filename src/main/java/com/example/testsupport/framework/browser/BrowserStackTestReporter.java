package com.example.testsupport.framework.browser;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Minimal reporter that updates BrowserStack Automate session status.
 */
public class BrowserStackTestReporter {

    private final String username = System.getenv("BROWSERSTACK_USERNAME");
    private final String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

    public void testPassed(String sessionId) {
        update(sessionId, "passed", null);
    }

    public void testFailed(String sessionId, String reason) {
        update(sessionId, "failed", reason);
    }

    private void update(String sessionId, String status, String reason) {
        if (sessionId == null || username == null || accessKey == null) {
            return;
        }
        try {
            URL url = new URL("https://api.browserstack.com/automate/sessions/" + sessionId + ".json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            String auth = username + ":" + accessKey;
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encoded);
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject payload = new JSONObject();
            payload.put("status", status);
            if (reason != null) {
                payload.put("reason", reason);
            }

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }
            conn.getInputStream().close();
        } catch (Exception ignored) {
        }
    }
}

