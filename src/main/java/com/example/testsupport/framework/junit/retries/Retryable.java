package com.example.testsupport.framework.junit.retries;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {
    int attempts() default 3;
    Class<? extends Throwable>[] onExceptions() default {Throwable.class};
}

