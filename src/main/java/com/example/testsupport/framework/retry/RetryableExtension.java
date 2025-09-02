package com.example.testsupport.framework.retry;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class RetryableExtension implements InvocationInterceptor {

    private boolean isRetryable(Throwable throwable, Class<? extends Throwable>[] retryable) {
        if (throwable == null) return false;
        for (Class<? extends Throwable> ex : retryable) {
            if (ex.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }
        return isRetryable(throwable.getCause(), retryable);
    }

    private String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private void execute(Invocation<Void> invocation, Retryable annotation, ReflectiveInvocationContext<Method> invCtx, ExtensionContext context) throws Throwable {
        int repeats = annotation.repeats();
        long suspend = annotation.suspend();
        Class<? extends Throwable>[] exceptions = annotation.onExceptions();
        Object target = context.getRequiredTestInstance();
        Throwable lastError = null;
        for (int attempt = 1; attempt <= repeats + 1; attempt++) {
            try {
                String stepName = attempt == 1 ? "Test execution" : String.format("Attempt %d of %d", attempt, repeats + 1);
                int currentAttempt = attempt;
                Allure.step(stepName, () -> {
                    if (currentAttempt == 1) {
                        invocation.proceed();
                    } else {
                        try {
                            invCtx.getExecutable().invoke(target, invCtx.getArguments().toArray());
                        } catch (InvocationTargetException ite) {
                            throw ite.getTargetException();
                        }
                    }
                });
                return;
            } catch (Throwable t) {
                lastError = t;
                if (!isRetryable(t, exceptions) || attempt > repeats) {
                    throw t;
                }
                Allure.addAttachment("Failed on attempt " + attempt, stackTrace(t));
                if (suspend > 0) {
                    try {
                        Thread.sleep(suspend);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        throw lastError;
    }

    private void intercept(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invCtx, ExtensionContext context) throws Throwable {
        Optional<Method> method = context.getTestMethod();
        if (method.isPresent()) {
            Retryable annotation = method.get().getAnnotation(Retryable.class);
            if (annotation != null) {
                execute(invocation, annotation, invCtx, context);
                return;
            }
        }
        invocation.proceed();
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext context) throws Throwable {
        intercept(invocation, invocationContext, context);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext context) throws Throwable {
        intercept(invocation, invocationContext, context);
    }
}
