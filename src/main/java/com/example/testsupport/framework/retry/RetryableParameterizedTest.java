package com.example.testsupport.framework.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ArgumentsProvider;

/**
 * Marker annotation that enables retry logic for parameterised tests.
 * <p>
 * At this stage the underlying extension provides a single execution for each
 * argument set supplied by {@link #source()} without any retry behaviour.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(RetryableParameterizedTestExtension.class)
public @interface RetryableParameterizedTest {
    /** Number of retry attempts after the first run. */
    int repeats() default 1;

    /** Minimum number of successful executions required. */
    int minSuccess() default 1;

    /** Delay in milliseconds between attempts. */
    long suspend() default 0L;

    /** Exceptions that trigger a retry. */
    Class<? extends Throwable>[] exceptions() default {Throwable.class};

    /**
     * Display name for individual invocations, following the same rules as
     * {@code @ParameterizedTest}.
     */
    String name() default "[{index}] {arguments}";

    /**
     * Display name for repeated attempts. Not used in the current stub
     * implementation but kept for future development.
     */
    String repeatedName() default "{displayName} (retry {currentRepetition}/{totalRepetitions})";

    /** Provides arguments for the test. */
    Class<? extends ArgumentsProvider> source();
}
