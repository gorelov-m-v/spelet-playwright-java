package com.example.testsupport.framework.retry;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Extension that provides retry support for {@link RetryableParameterizedTest}.
 * Each parameterized invocation is retried independently.
 */
public class RetryableParameterizedTestExtension implements InvocationInterceptor {

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
                                            ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) throws Throwable {
        RetryableParameterizedTest annotation = extensionContext.getRequiredTestMethod()
                .getAnnotation(RetryableParameterizedTest.class);
        if (annotation == null) {
            invocation.proceed();
            return;
        }
        RetryConfig config = RetryConfig.from(annotation);
        Object testInstance = extensionContext.getRequiredTestInstance();
        Method method = extensionContext.getRequiredTestMethod();
        AtomicInteger attempt = new AtomicInteger();
        RetryExecutor.execute(() -> {
            int current = attempt.getAndIncrement();
            try {
                if (current == 0) {
                    invocation.proceed();
                } else {
                    method.invoke(testInstance, invocationContext.getArguments().toArray());
                }
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }, config);
    }
}
