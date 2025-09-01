package com.example.testsupport.framework.retry;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Executes a callback with retry semantics defined by {@link RetryConfig}.
 */
final class RetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(RetryExecutor.class);

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

                int currentAttempt = attempts + 1;
                log.warn("[RETRY] Attempt {}/{} failed with '{}: {}'. Retrying...",
                        currentAttempt, totalAttempts, t.getClass().getSimpleName(), t.getMessage());

                try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                    t.printStackTrace(pw);
                    Allure.addAttachment("Stacktrace on attempt " + currentAttempt, sw.toString());
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
