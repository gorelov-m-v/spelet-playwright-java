package com.example.testsupport.framework.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Вспомогательный класс для отслеживания успешных запусков
 * параметризованных тестов с ретраями. Необходим для работы с @CartesianTest.
 */
public class SuccessTracker {
    private static final Map<String, Boolean> successMap = new ConcurrentHashMap<>();

    public static void markAsSuccess(String testCaseId) {
        successMap.put(testCaseId, true);
    }

    public static boolean isSuccessful(String testCaseId) {
        return successMap.getOrDefault(testCaseId, false);
    }

    public static void clear() {
        successMap.clear();
    }
}
