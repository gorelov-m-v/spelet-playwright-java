package com.example.testsupport.framework.browser;

import org.springframework.stereotype.Component;

/**
 * Manages BrowserStack session ids per executing thread.
 */
@Component
public class BrowserStackSessionManager {
    private static final ThreadLocal<String> sessionId = new ThreadLocal<>();

    public void setSessionId(String id) {
        sessionId.set(id);
    }

    public String getSessionId() {
        return sessionId.get();
    }

    public void clear() {
        sessionId.remove();
    }
}
