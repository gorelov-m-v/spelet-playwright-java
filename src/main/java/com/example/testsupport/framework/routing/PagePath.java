package com.example.testsupport.framework.routing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания относительного пути к странице.
 * Используется системой роутинга для построения полных URL.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PagePath {
    /**
     * Относительный путь к странице.
     * @return например, "/casino"
     */
    String value();
}
