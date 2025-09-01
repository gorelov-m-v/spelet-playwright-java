package tests.framework;

import com.example.testsupport.config.EnvironmentConfig;
import com.example.testsupport.framework.device.TestMatrixService;
import com.example.testsupport.framework.junit.retries.RetryableParameterizedTest;
import com.example.testsupport.framework.junit.retries.RetryableTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Demonstrates retry behaviour of {@link RetryableTest} and {@link RetryableParameterizedTest}.
 * Contains a flaky test that eventually passes, a test that always fails,
 * and a parameterized set of scenarios executed in parallel to surface
 * race conditions in the retry mechanism.
 */
@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RetryLogicTest.TestConfig.class)
public class RetryLogicTest {

    private static final AtomicInteger flakyAttempts = new AtomicInteger();
    private static final AtomicInteger failAttempts = new AtomicInteger();
    private static final Map<String, AtomicInteger> parameterizedTestCounters = new ConcurrentHashMap<>();

    @BeforeEach
    void resetCounters() {
        parameterizedTestCounters.clear();
    }

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

    /**
     * Exercises retry behaviour under concurrent, parameterized execution.
     * Each scenario may succeed immediately, succeed after a number of failures,
     * or keep failing for all attempts.
     *
     * @param scenarioName          unique scenario identifier
     * @param failuresBeforeSuccess number of failures before success; -1 means the scenario always fails
     */
    @RetryableParameterizedTest(repeats = 3, exceptions = {AssertionError.class})
    @ArgumentsSource(FlakyTestArgumentProvider.class)
    void parameterizedRetryTest(String scenarioName, int failuresBeforeSuccess) {
        AtomicInteger counter = parameterizedTestCounters.computeIfAbsent(scenarioName, s -> new AtomicInteger());
        int attempt = counter.incrementAndGet();
        System.out.printf("%s attempt #%d%n", scenarioName, attempt);
        if (failuresBeforeSuccess == -1 || attempt <= failuresBeforeSuccess) {
            Assertions.fail("Scenario " + scenarioName + " failed on attempt " + attempt);
        }
    }

    /**
     * Minimal Spring context supplying a stubbed {@link TestMatrixService}
     * so the built-in {@link com.example.testsupport.framework.device.DeviceProvider}
     * used by {@link RetryableParameterizedTest} can load without external dependencies.
     */
    @Configuration
    static class TestConfig {
        @Bean
        TestMatrixService testMatrixService() {
            return new TestMatrixService(new EnvironmentConfig()) {
                @Override
                public Stream<List<Object>> getTestMatrix() {
                    return Stream.empty();
                }
            };
        }
    }
}

