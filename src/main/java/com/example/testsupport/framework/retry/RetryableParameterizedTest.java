package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation enabling retry mechanism for parameterized tests.
 * <p>
 * This annotation deliberately does <strong>not</strong> declare
 * {@code org.junit.jupiter.params.provider.ArgumentsSource} to ensure that
 * data providers are specified explicitly on each test method and to avoid
 * recursive parameter resolution.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(RetryableParameterizedTestExtension.class)
public @interface RetryableParameterizedTest {
    int repeats() default 1;
    int minSuccess() default 1;
    long suspend() default 0L;
    Class<? extends Throwable>[] exceptions() default {Throwable.class};

    String name() default "[{index}] {arguments}";
    String repeatedName() default "{displayName} (retry {currentRepetition}/{totalRepetitions})";
}
