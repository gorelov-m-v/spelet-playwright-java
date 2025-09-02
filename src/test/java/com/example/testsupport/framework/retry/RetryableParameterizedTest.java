package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to enable retry logic for parameterized tests.
 * Data source should be provided explicitly via {@link #source()}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(RetryableParameterizedTestExtension.class)
public @interface RetryableParameterizedTest {
    int repeats() default 1;
    int minSuccess() default 1;
    long suspend() default 0L;
    Class<? extends Throwable>[] exceptions() default { Throwable.class };

    /**
     * Display name pattern for each set of parameters.
     */
    String name() default "[{index}] {arguments}";

    /**
     * Display name pattern for repeated attempts.
     */
    String repeatedName() default "{displayName} (retry {currentRepetition}/{totalRepetitions})";

    /**
     * Provider class that supplies arguments for the parameterized test.
     */
    Class<? extends ArgumentsProvider> source();
}
