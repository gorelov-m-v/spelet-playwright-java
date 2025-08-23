package com.example.testsupport.pages.components;

import com.microsoft.playwright.Locator;

/**
 * Базовый класс для всех UI-компонентов.
 * Каждый компонент ограничивается своим корневым локатором.
 */
public abstract class BaseComponent {
    protected final Locator root;

    protected BaseComponent(Locator root) {
        this.root = root;
    }

    protected Locator root() {
        return root;
    }
}
