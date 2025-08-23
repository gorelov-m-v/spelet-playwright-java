package com.example.testsupport.framework.device;

/**
 * Описание устройства для тестирования, включая его имя и размер в пикселях.
 */
public record Device(String name, int width, int height) {
    @Override
    public String toString() {
        return name;
    }
}
