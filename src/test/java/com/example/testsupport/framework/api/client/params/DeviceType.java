package com.example.testsupport.framework.api.client.params;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of supported device types for API requests.
 */
public enum DeviceType {
    MOBILE("mobile"),
    DESKTOP("desktop");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
