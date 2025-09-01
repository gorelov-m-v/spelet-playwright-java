package com.example.testsupport.framework.retry;

import java.util.Arrays;

/**
 * Simple immutable configuration holder for retry settings.
 */
public record RetryConfig(int repeats, int minSuccess, long suspend,
                          Class<? extends Throwable>[] retryOn) {

    static RetryConfig from(RetryableTest annotation) {
        int repeats = getInt("retry.repeats", annotation.repeats());
        int minSuccess = getInt("retry.minSuccess", annotation.minSuccess());
        long suspend = getLong("retry.suspend", annotation.suspend());
        return new RetryConfig(repeats, minSuccess, suspend, annotation.retryOn());
    }

    static RetryConfig from(RetryableParameterizedTest annotation) {
        int repeats = getInt("retry.repeats", annotation.repeats());
        int minSuccess = getInt("retry.minSuccess", annotation.minSuccess());
        long suspend = getLong("retry.suspend", annotation.suspend());
        return new RetryConfig(repeats, minSuccess, suspend, annotation.retryOn());
    }

    private static int getInt(String key, int defaultValue) {
        String val = System.getProperty(key);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    private static long getLong(String key, long defaultValue) {
        String val = System.getProperty(key);
        return val != null ? Long.parseLong(val) : defaultValue;
    }

    boolean isRetryable(Throwable t) {
        return Arrays.stream(retryOn)
                .anyMatch(cls -> isInstanceOf(t, cls));
    }

    private boolean isInstanceOf(Throwable t, Class<? extends Throwable> cls) {
        Throwable current = t;
        while (current != null) {
            if (cls.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
