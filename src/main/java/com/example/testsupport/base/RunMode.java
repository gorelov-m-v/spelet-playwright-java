package com.example.testsupport.base;

public enum RunMode {
    LOCAL,
    BROWSERSTACK;

    public static RunMode fromProperty(String prop) {
        if (prop == null) return LOCAL;
        return "browserstack".equalsIgnoreCase(prop) ? BROWSERSTACK : LOCAL;
    }
}
