package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(RetryableParameterizedTestExtension.class)
public @interface RetryableParameterizedTest {
    int repeats() default 1; // total attempts
    int minSuccess() default 1;
    long suspend() default 0L;
    Class<? extends Throwable>[] exceptions() default {Throwable.class};
    String name() default "[{index}] {arguments}";
    String repeatedName() default "{displayName} (retry {currentRepetition}/{totalRepetitions})";
}
