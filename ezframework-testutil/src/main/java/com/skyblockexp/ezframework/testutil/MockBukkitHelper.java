package com.skyblockexp.ezframework.testutil;

import java.lang.reflect.Method;

/** Reflection-based helper for MockBukkit operations so test modules don't need to
 * depend on MockBukkit at compile-time.
 */
public final class MockBukkitHelper {
    private MockBukkitHelper() {}

    private static Class<?> findMockBukkit() {
        String name = "org.mockbukkit.mockbukkit.MockBukkit";
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            return Class.forName(name, true, cl);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MockBukkit not found on classpath", e);
        }
    }

    public static Object mock() {
        try {
            Class<?> mb = findMockBukkit();
            Method m = mb.getMethod("mock");
            return m.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object load(Class<?> pluginClass) {
        try {
            Class<?> mb = findMockBukkit();
            Method m = mb.getMethod("load", Class.class);
            return m.invoke(null, pluginClass);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unmock() {
        try {
            Class<?> mb = findMockBukkit();
            Method m = mb.getMethod("unmock");
            m.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
