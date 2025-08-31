package com.example.testsupport.annotations;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(RetryableTestExtension.class)
public @interface RetryableTest {

    String DISPLAY_NAME_PLACEHOLDER = "{displayName}";
    String CURRENT_REPETITION_PLACEHOLDER = "{currentRepetition}";
    String TOTAL_REPETITIONS_PLACEHOLDER = "{totalRepetitions}";
    String SHORT_DISPLAY_NAME = "Repetition " + CURRENT_REPETITION_PLACEHOLDER + " of " + TOTAL_REPETITIONS_PLACEHOLDER;
    String LONG_DISPLAY_NAME = DISPLAY_NAME_PLACEHOLDER + " :: " + SHORT_DISPLAY_NAME;

    Class<? extends Throwable>[] exceptions() default Throwable.class;

    int repeats() default 1;

    int minSuccess() default 1;

    long suspend() default 0L;

    String name() default SHORT_DISPLAY_NAME;
}
