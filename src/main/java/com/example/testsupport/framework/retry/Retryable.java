package com.example.testsupport.framework.retry;

import java.lang.annotation.*;

/**
 * Включает механизм перезапуска для любого теста JUnit 5 (@Test, @ParameterizedTest, etc.).
 * Должна использоваться вместе с {@code @ExtendWith(RetryableExtension.class)} на уровне класса или метода.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {
    /** Количество перезапусков после первой неудачной попытки. */
    int repeats() default 1;
    /** Задержка в миллисекундах перед следующим перезапуском. */
    long suspend() default 0L;
    /** Массив типов исключений, которые вызывают перезапуск. */
    Class<? extends Throwable>[] onExceptions() default {Throwable.class};
}
