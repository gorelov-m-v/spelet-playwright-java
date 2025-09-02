package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that enables retry logic for standard tests.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(RetryableTestExtension.class)
public @interface RetryableTest {
    /**
     * Number of additional attempts after the first run.
     */
    int repeats() default 1;

    /**
     * Minimum number of successful executions required to mark test as passed.
     */
    int minSuccess() default 1;

    /**
     * Delay between attempts in milliseconds.
     */
    long suspend() default 0L;

    /**
     * Exception types that should trigger a retry.
     */
    Class<? extends Throwable>[] exceptions() default { Throwable.class };
}
