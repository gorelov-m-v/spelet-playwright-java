package com.example.testsupport.framework.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Marker annotation that enables retry logic for a regular JUnit test.
 * <p>
 * At this stage the corresponding extension simply executes the test once
 * without any retry behaviour. Full retry capabilities will be implemented in
 * further development stages.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(RetryableTestExtension.class)
public @interface RetryableTest {
    /** Number of retry attempts after the first run. */
    int repeats() default 1;

    /** Minimum number of successful executions required. */
    int minSuccess() default 1;

    /** Delay in milliseconds between attempts. */
    long suspend() default 0L;

    /** Exceptions that trigger a retry. */
    Class<? extends Throwable>[] exceptions() default {Throwable.class};
}
