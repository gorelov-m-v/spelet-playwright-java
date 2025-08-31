package com.example.testsupport.framework.junit.retries;

import com.example.testsupport.framework.device.DeviceProvider;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ArgumentsSource(DeviceProvider.class)
@ExtendWith(RetryableParameterizedTestExtension.class)
public @interface RetryableParameterizedTest {

    String CURRENT_REPETITION_PLACEHOLDER = "{currentRepetition}";
    String TOTAL_REPETITIONS_PLACEHOLDER = "{totalRepetitions}";
    String REPEATED_DISPLAY_NAME = " (Repeated if the test failed " + CURRENT_REPETITION_PLACEHOLDER + " of " + TOTAL_REPETITIONS_PLACEHOLDER + ")";
    String DISPLAY_NAME_PLACEHOLDER = "{displayName}";
    String INDEX_PLACEHOLDER = "{index}";
    String ARGUMENTS_PLACEHOLDER = "{arguments}";
    String DEFAULT_DISPLAY_NAME = "[" + INDEX_PLACEHOLDER + "] " + ARGUMENTS_PLACEHOLDER;

    String name() default DEFAULT_DISPLAY_NAME;

    String repeatedName() default REPEATED_DISPLAY_NAME;

    Class<? extends Throwable>[] exceptions() default {Throwable.class};

    int repeats() default 1;

    int minSuccess() default 1;

    long suspend() default 0L;
}
