package com.example.testsupport.framework.junit.retries;

import io.qameta.allure.Allure;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

@Aspect
@Component
public class RetryAspect {

    @Around("@annotation(retryable)")
    public Object around(ProceedingJoinPoint joinPoint, Retryable retryable) throws Throwable {
        int attempts = retryable.attempts();
        Class<? extends Throwable>[] retryOn = retryable.onExceptions();
        Throwable last = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                int current = attempt;
                return Allure.step(String.format("Попытка %d из %d", current, attempts), () -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable t) {
                        Allure.addAttachment("Ошибка", stackTrace(t));
                        throw t;
                    }
                });
            } catch (Throwable t) {
                last = t;
                boolean shouldRetry = Arrays.stream(retryOn)
                        .anyMatch(ex -> ex.isAssignableFrom(t.getClass()));
                if (!shouldRetry || attempt == attempts) {
                    throw t;
                }
            }
        }
        throw last;
    }

    private String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

