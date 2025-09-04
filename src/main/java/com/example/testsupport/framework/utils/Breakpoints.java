package com.example.testsupport.framework.utils;

/**
 * Common viewport breakpoints used in tests for responsive behavior.
 */
public final class Breakpoints {
    private Breakpoints() {}

    /**
     * Large desktop width.
     */
    public static final int DESKTOP_LARGE = 1280;

    /**
     * Medium desktop or laptop width.
     */
    public static final int DESKTOP_MEDIUM = 940;

    /**
     * Width below which layout switches to mobile navigation.
     */
    public static final int MOBILE = 960;

    /**
     * Width below which certain buttons collapse to icon-only variants.
     */
    public static final int TABLET = 768;

    /**
     * Small mobile devices width.
     */
    public static final int MOBILE_SMALL = 480;
}
