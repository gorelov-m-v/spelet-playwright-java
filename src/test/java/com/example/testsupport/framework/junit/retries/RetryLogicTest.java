package com.example.testsupport.framework.junit.retries;

import com.example.testsupport.TestApplication;
import com.example.testsupport.framework.junit.retries.FlakyTestArgumentProvider.Scenario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootTest(classes = {TestApplication.class, RetryLogicTest.Config.class})
class RetryLogicTest {

    static class DemoService {
        private int flakyAttempts;
        private int failAttempts;
        private int nonRetryAttempts;

        void resetFlaky() { flakyAttempts = 0; }
        void resetFail() { failAttempts = 0; }
        void resetNonRetry() { nonRetryAttempts = 0; }

        int getFlakyAttempts() { return flakyAttempts; }
        int getFailAttempts() { return failAttempts; }
        int getNonRetryAttempts() { return nonRetryAttempts; }

        @Retryable(attempts = 3)
        void flakyOperation() {
            flakyAttempts++;
            if (flakyAttempts < 3) {
                throw new RuntimeException("flaky");
            }
        }

        @Retryable(attempts = 3)
        void alwaysFailOperation() {
            failAttempts++;
            throw new RuntimeException("always fails");
        }

        @Retryable(onExceptions = {IOException.class})
        void nonRetryableOperation() {
            nonRetryAttempts++;
            throw new AssertionError("boom");
        }
    }

    @Autowired
    DemoService service;

    @TestConfiguration
    static class Config {
        @Bean
        DemoService demoService() {
            return new DemoService();
        }
    }

    @Test
    void flakyTestPassesAfterRetries() {
        service.resetFlaky();
        service.flakyOperation();
        Assertions.assertEquals(3, service.getFlakyAttempts());
    }

    @ParameterizedTest
    @ArgumentsSource(FlakyTestArgumentProvider.class)
    @Retryable(attempts = 3)
    void parameterizedTest(Scenario scenario) {
        switch (scenario) {
            case SUCCESS -> { /* do nothing */ }
            case FLAKY -> {
                service.resetFlaky();
                service.flakyOperation();
                Assertions.assertEquals(3, service.getFlakyAttempts());
            }
            case FAIL -> {
                service.resetFail();
                Assertions.assertThrows(RuntimeException.class, service::alwaysFailOperation);
                Assertions.assertEquals(3, service.getFailAttempts());
            }
        }
    }

    @Test
    void exceptionFilteringWorks() {
        service.resetNonRetry();
        Assertions.assertThrows(AssertionError.class, service::nonRetryableOperation);
        Assertions.assertEquals(1, service.getNonRetryAttempts());
    }
}

