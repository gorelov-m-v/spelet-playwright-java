package com.example.testsupport.framework.browser;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Simple reporter that updates BrowserStack session status via REST API.
 */
public class BrowserStackTestReporter {
    private final String username = System.getenv("BROWSERSTACK_USERNAME");
    private final String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
    private final HttpClient client = HttpClient.newHttpClient();

    private void send(String sessionId, String status, String reason) {
        try {
            String url = "https://api.browserstack.com/automate/sessions/" + sessionId + ".json";
            JSONObject body = new JSONObject();
            body.put("status", status);
            if (reason != null) {
                body.put("reason", reason);
            }
            String auth = Base64.getEncoder().encodeToString((username + ":" + accessKey).getBytes(StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
            // ignore failures to report status
        }
    }

    public void testPassed(String sessionId) {
        send(sessionId, "passed", null);
    }

    public void testFailed(String sessionId, String reason) {
        send(sessionId, "failed", reason);
    }
}
