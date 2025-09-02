package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(RetryableTestExtension.class)
public @interface RetryableTest {
    int repeats() default 1; // number of retries
    int minSuccess() default 1;
    long suspend() default 0L;
    Class<? extends Throwable>[] exceptions() default {Throwable.class};
}
