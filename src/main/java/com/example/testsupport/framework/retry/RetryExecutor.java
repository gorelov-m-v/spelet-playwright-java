package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.function.ThrowingSupplier;

/**
 * Executes a callback with retry semantics defined by {@link RetryConfig}.
 */
final class RetryExecutor {

    private RetryExecutor() {}

    static void execute(ThrowingRunnable action, RetryConfig config) throws Throwable {
        int attempts = 0;
        int successes = 0;
        Throwable lastError = null;
        int totalAttempts = config.repeats() + 1;

        while (attempts < totalAttempts && successes < config.minSuccess()) {
            try {
                action.run();
                successes++;
            } catch (Throwable t) {
                lastError = t;
                if (!config.isRetryable(t) || attempts >= config.repeats()) {
                    throw t;
                }
                if (config.suspend() > 0) {
                    try {
                        Thread.sleep(config.suspend());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ie;
                    }
                }
            }
            attempts++;
        }

        if (successes < config.minSuccess() && lastError != null) {
            throw lastError;
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
