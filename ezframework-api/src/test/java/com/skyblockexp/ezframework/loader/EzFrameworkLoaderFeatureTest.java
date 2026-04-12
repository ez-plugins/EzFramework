package com.skyblockexp.ezframework.loader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EzFrameworkLoaderFeatureTest {

    /**
     * In a test-classpath environment the EzFramework manager plugin class
     * ({@code com.skyblockexp.ezframework.plugin.EzFrameworkPlugin}) is never
     * present, so {@code isManagerPresent()} must return {@code false}.
     */
    @Test
    void isManagerPresentReturnsFalseWhenManagerNotOnClasspath() {
        assertFalse(EzFrameworkLoader.isManagerPresent());
    }

    /**
     * Repeated calls must be stable — the method must not flip or throw.
     */
    @Test
    void isManagerPresentIsIdempotent() {
        boolean first  = EzFrameworkLoader.isManagerPresent();
        boolean second = EzFrameworkLoader.isManagerPresent();
        assertEquals(first, second);
    }
}
