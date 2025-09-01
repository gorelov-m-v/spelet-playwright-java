package tests.framework;

import com.example.testsupport.framework.junit.retries.RetryableTest;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates retry behaviour of {@link RetryableTest}.
 * Contains a flaky test that eventually passes and a test that
 * always fails even after all retry attempts.
 */
public class RetryLogicTest {

    private static final AtomicInteger flakyAttempts = new AtomicInteger();
    private static final AtomicInteger failAttempts = new AtomicInteger();

    /**
     * Fails on the first attempt and succeeds on the second, verifying
     * that a failing run can be retried and eventually pass.
     */
    @RetryableTest(repeats = 2, exceptions = {AssertionError.class})
    void flakyTestShouldPassOnSuccessfulRetry() {
        int attempt = flakyAttempts.incrementAndGet();
        System.out.println("flakyTest attempt #" + attempt);
        if (attempt < 2) {
            Assertions.fail("Flaky failure on attempt " + attempt);
        }
    }

    /**
     * Always fails, demonstrating that a test remains failed after
     * exhausting all retry attempts.
     */
    @RetryableTest(repeats = 2, exceptions = {AssertionError.class})
    void testThatAlwaysFailsShouldRemainFailed() {
        int attempt = failAttempts.incrementAndGet();
        System.out.println("alwaysFailTest attempt #" + attempt);
        Assertions.fail("Persistent failure on attempt " + attempt);
    }
}

