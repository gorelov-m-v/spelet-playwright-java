package test.pages;

import main.framework.localization.LocalizationService;
import test.pages.components.HeaderComponent;
import test.pages.components.TabBarComponent;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.ObjectProvider;
import java.util.function.Supplier;

/**
 * Base Page Object with shared logic.
 */
public abstract class BasePage<T extends BasePage<T>> {
    private final ObjectProvider<Page> pageProvider;
    protected final LocalizationService ls;
    private final Supplier<HeaderComponent> header;
    private final Supplier<TabBarComponent> tabBar;

    @SuppressWarnings("resource")
    protected BasePage(ObjectProvider<Page> pageProvider, LocalizationService ls) {
        this.pageProvider = pageProvider;
        this.ls = ls;
        this.header = memoize(() -> new HeaderComponent(page().locator("header"), ls));
        this.tabBar = memoize(() -> new TabBarComponent(page().locator("div.tab-bar__list"), ls));
    }

    protected Page page() {
        return pageProvider.getObject();
    }

    public HeaderComponent header() {
        return header.get();
    }

    public TabBarComponent tabBar() {
        return tabBar.get();
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    private static <T> Supplier<T> memoize(Supplier<T> supplier) {
        return new Supplier<>() {
            private T value;
            @Override
            public T get() {
                if (value == null) {
                    value = supplier.get();
                }
                return value;
            }
        };
    }

    /**
     * Checks that the current URL contains the expected path.
     *
     * @param expectedPath expected URL substring
     */
    public void verifyUrlContains(String expectedPath) {
        String current = page().url();
        Assertions.assertTrue(
                current.contains(expectedPath),
                String.format("Expected URL to contain '%s' but was '%s'", expectedPath, current)
        );
    }

    public abstract T verifyIsLoaded();
}
