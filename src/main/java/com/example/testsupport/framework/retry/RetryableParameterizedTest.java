package com.example.testsupport.framework.retry;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import java.lang.annotation.*;

/**
 * Parameterized variant of {@link RetryableTest}. Each invocation with a distinct
 * set of arguments is retried independently.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
@ExtendWith(RetryableParameterizedTestExtension.class)
public @interface RetryableParameterizedTest {
    int repeats() default 0;
    int minSuccess() default 1;
    long suspend() default 0L;
    Class<? extends Throwable>[] retryOn() default { Throwable.class };
}
