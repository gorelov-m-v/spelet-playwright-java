package com.example.testsupport.framework.retry;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {
    int repeats() default 1;
    long suspend() default 0L;
    Class<? extends Throwable>[] onExceptions() default {Throwable.class};
}
