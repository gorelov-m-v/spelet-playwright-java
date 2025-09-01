package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.lang.annotation.*;

/**
 * Annotation that marks a regular JUnit test as retryable. Configuration can be
 * provided via attributes or overridden using system properties with the prefix
 * {@code retry.}. For example {@code -Dretry.repeats=2}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@ExtendWith(RetryableTestExtension.class)
public @interface RetryableTest {
    /** Number of retries to perform after the initial attempt. */
    int repeats() default 0;

    /** Minimum number of successful executions required for the test to pass. */
    int minSuccess() default 1;

    /** Delay in milliseconds before the next retry attempt. */
    long suspend() default 0L;

    /** Exception types that should trigger retry logic. */
    Class<? extends Throwable>[] retryOn() default { Throwable.class };
}
