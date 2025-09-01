package com.example.testsupport.framework.utils;

import io.qameta.allure.Allure;

public final class AllureHelper {
    private AllureHelper() {}

    public static <T> T step(String name, Allure.ThrowableRunnable<T> runnable) {
        return Allure.step(name, runnable);
    }

    public static void step(String name, Allure.ThrowableRunnableVoid runnable) {
        Allure.step(name, runnable);
    }
}
