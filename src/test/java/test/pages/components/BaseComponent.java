package test.pages.components;

import com.microsoft.playwright.Locator;

/**
 * Base class for all UI components.
 * Each component is scoped by its root locator.
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
