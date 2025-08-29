package test.framework.utils;

/**
 * Common viewport breakpoints used in tests for responsive behavior.
 */
public final class Breakpoints {
    private Breakpoints() {}

    /**
     * Width below which layout switches to mobile navigation.
     */
    public static final int MOBILE = 960;

    /**
     * Width below which certain buttons collapse to icon-only variants.
     */
    public static final int TABLET = 768;
}
