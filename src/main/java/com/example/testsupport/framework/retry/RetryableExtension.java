package com.example.testsupport.framework.retry;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.Optional;

public class RetryableExtension implements InvocationInterceptor {

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        handleInvocation(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation,
                                            ReflectiveInvocationContext<Method> invocationContext,
                                            ExtensionContext extensionContext) throws Throwable {
        // отдельная обработка для параметризованных тестов
        handleInvocation(invocation, invocationContext, extensionContext);
    }

    private void handleInvocation(Invocation<Void> invocation,
                                  ReflectiveInvocationContext<Method> invocationContext,
                                  ExtensionContext extensionContext) throws Throwable {
        Optional<Retryable> annotation = findAnnotation(extensionContext);

        int configuredRepeats = resolveConfiguredRepeats(extensionContext);

        if (annotation.isEmpty()) {
            if (configuredRepeats > 0) {
                Retryable retryable = new Retryable() {
                    @Override
                    public int repeats() {
                        return configuredRepeats;
                    }

                    @Override
                    public long suspend() {
                        return 0L;
                    }

                    @Override
                    public Class<? extends Throwable>[] onExceptions() {
                        return new Class[]{Throwable.class};
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Retryable.class;
                    }
                };
                executeWithRetries(invocation, invocationContext, extensionContext, retryable);
            } else {
                invocation.proceed();
            }
            return;
        }

        executeWithRetries(invocation, invocationContext, extensionContext, annotation.get());
    }

    private void executeWithRetries(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext,
                                    Retryable annotation) throws Throwable {
        int repeats = annotation.repeats();
        long suspend = annotation.suspend();
        Class<? extends Throwable>[] exceptions = annotation.onExceptions();
        Throwable lastError = null;

        Method method = invocationContext.getExecutable();
        Object target = extensionContext.getRequiredTestInstance();
        Object[] args = invocationContext.getArguments().toArray();

        for (int attempt = 1; attempt <= repeats + 1; attempt++) {
            try {
                String stepName = String.format("Попытка %d из %d", attempt, repeats + 1);
                String stepId = java.util.UUID.randomUUID().toString();
                Allure.getLifecycle().startStep(stepId, new StepResult().setName(stepName));
                try {
                    if (attempt == 1) {
                        invocation.proceed();
                    } else {
                        method.invoke(target, args);
                    }
                    Allure.getLifecycle().updateStep(stepId, s -> s.setStatus(Status.PASSED));
                } catch (Throwable t) {
                    Allure.getLifecycle().updateStep(stepId, s -> s.setStatus(Status.FAILED));
                    throw unwrapInvocationTargetException(t);
                } finally {
                    Allure.getLifecycle().stopStep(stepId);
                }
                return; // Успех! Выходим.
            } catch (Throwable t) {
                lastError = t;
                if (!isExceptionRetryable(t, exceptions) || attempt > repeats) {
                    throw t; // Попытки кончились или не тот тип ошибки - падаем
                }

                // Логируем ошибку и готовимся к перезапуску
                String stackTrace = getStackTrace(t);
                Allure.addAttachment(String.format("Ошибка на попытке %d", attempt), stackTrace);
                System.err.printf("[RETRY] Попытка %d провалена: %s. Перезапускаем...%n", attempt, t.getMessage());

                if (suspend > 0) {
                    Thread.sleep(suspend);
                }
            }
        }
        throw lastError;
    }

    private Throwable unwrapInvocationTargetException(Throwable t) {
        if (t instanceof java.lang.reflect.InvocationTargetException ex && ex.getTargetException() != null) {
            return ex.getTargetException();
        }
        return t;
    }

    // --- Вспомогательные методы ---
    private Optional<Retryable> findAnnotation(ExtensionContext context) {
        return context.getElement()
            .map(el -> el.getAnnotation(Retryable.class));
    }

    private int resolveConfiguredRepeats(ExtensionContext context) {
        String sys = System.getProperty("test.retry");
        if (sys != null && !sys.isBlank()) {
            return Integer.parseInt(sys);
        }

        try {
            ApplicationContext spring = SpringExtension.getApplicationContext(context);
            String prop = spring.getEnvironment().getProperty("test.retry");
            if (prop != null && !prop.isBlank()) {
                return Integer.parseInt(prop);
            }
        } catch (Exception ignored) {
            // spring context might be unavailable
        }

        String profile = java.util.Optional.ofNullable(System.getenv("SPRING_PROFILES_ACTIVE"))
                .map(String::trim)
                .orElse("");
        String resource = "application" + (profile.isEmpty() ? "" : "-" + profile) + ".yml";
        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (is != null) {
                org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
                Object obj = yaml.load(is);
                if (obj instanceof java.util.Map<?,?> map) {
                    Object test = map.get("test");
                    if (test instanceof java.util.Map<?,?> tmap) {
                        Object retry = tmap.get("retry");
                        if (retry != null) {
                            return Integer.parseInt(retry.toString());
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore
        }

        return 0;
    }

    private boolean isExceptionRetryable(Throwable throwable, Class<? extends Throwable>[] retryableExceptions) {
        if (throwable == null) return false;
        for (Class<? extends Throwable> ex : retryableExceptions) {
            if (ex.isAssignableFrom(throwable.getClass())) return true;
        }
        return isExceptionRetryable(throwable.getCause(), retryableExceptions);
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
