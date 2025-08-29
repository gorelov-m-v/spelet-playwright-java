package test.framework.utils;

import io.qameta.allure.Allure;

public final class AllureHelper {
    private AllureHelper() {}

    public static <T> T step(String name, Allure.ThrowableRunnable<T> runnable) {
        try {
            return Allure.step(name, runnable);
        } catch (Throwable e) {
            throw new RuntimeException("Allure step failed: " + name, e);
        }
    }

    public static void step(String name, Allure.ThrowableRunnableVoid runnable) {
        try {
            Allure.step(name, runnable);
        } catch (Throwable e) {
            throw new RuntimeException("Allure step failed: " + name, e);
        }
    }
}
