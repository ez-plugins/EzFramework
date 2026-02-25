package com.skyblockexp.ezframework.testutil;

import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test helpers that do NOT depend on the core module at compile time.
 * They create runtime proxies implementing core interfaces via reflection.
 */
public final class TestPlatformProviders {
    private static final Map<Object, AtomicBoolean> SAVED = new WeakHashMap<>();

    private TestPlatformProviders() {}

    public static Object createSavingProvider() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> providerIface = Class.forName("com.skyblockexp.ezframework.config.PlatformConfigProvider", true, cl);
            Class<?> ezConfigIface = Class.forName("com.skyblockexp.ezframework.config.EzConfig", true, cl);

            AtomicBoolean savedFlag = new AtomicBoolean(false);

            // EzConfig proxy that sets savedFlag on save()
            Object ezConfigProxy = Proxy.newProxyInstance(cl, new Class<?>[]{ezConfigIface}, (proxy, method, args) -> {
                // handle common Object methods explicitly (some JVMs deliver these differently)
                String m0 = method.getName();
                if ("hashCode".equals(m0)) return System.identityHashCode(proxy);
                if ("equals".equals(m0)) return proxy == args[0];
                if ("toString".equals(m0)) return "EzConfigProxy@" + System.identityHashCode(proxy);
                String name = method.getName();
                if ("save".equals(name)) {
                    savedFlag.set(true);
                    return null;
                }
                if ("getDataFolder".equals(name)) {
                    return Path.of("/tmp/testpp");
                }
                if ("getFileName".equals(name)) {
                    return "config.yml";
                }
                // other methods are no-ops
                return defaultReturn(method.getReturnType());
            });

            // Provider proxy: implements provide(plugin, registry)
            Object provider = Proxy.newProxyInstance(cl, new Class<?>[]{providerIface}, (proxy, method, args) -> {
                // handle common Object methods explicitly (some JVMs deliver these differently)
                String m = method.getName();
                if ("hashCode".equals(m)) return System.identityHashCode(proxy);
                if ("equals".equals(m)) return proxy == args[0];
                if ("toString".equals(m)) return "PlatformProviderProxy@" + System.identityHashCode(proxy);
                if ("provide".equals(method.getName())) {
                    Object registry = args[1]; // second arg is registry
                    // call registry.register(cfg.getFileName(), cfg) via reflection
                    Method register = findMethod(registry.getClass(), "register", String.class, Class.forName("com.skyblockexp.ezframework.config.EzConfig", true, cl));
                    if (register == null) {
                        // try with Object param fallback
                        register = findMethod(registry.getClass(), "register", String.class, Object.class);
                    }
                    if (register != null) {
                        register.setAccessible(true);
                        register.invoke(registry, "config.yml", ezConfigProxy);
                    }
                    // store saved flag for lookup
                    synchronized (SAVED) { SAVED.put(proxy, savedFlag); }
                    return null;
                }
                return null;
            });

            return provider;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object createRegisteringProvider(String fileName, Path folder) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> providerIface = Class.forName("com.skyblockexp.ezframework.config.PlatformConfigProvider", true, cl);
            Class<?> ezConfigIface = Class.forName("com.skyblockexp.ezframework.config.EzConfig", true, cl);

            Object ezConfigProxy = Proxy.newProxyInstance(cl, new Class<?>[]{ezConfigIface}, (proxy, method, args) -> {
                String name = method.getName();
                if ("getDataFolder".equals(name)) {
                    return folder;
                }
                if ("getFileName".equals(name)) {
                    return fileName;
                }
                return defaultReturn(method.getReturnType());
            });

            Object provider = Proxy.newProxyInstance(cl, new Class<?>[]{providerIface}, (proxy, method, args) -> {
                if ("provide".equals(method.getName())) {
                    Object registry = args[1];
                    Method register = findMethod(registry.getClass(), "register", String.class, Class.forName("com.skyblockexp.ezframework.config.EzConfig", true, cl));
                    if (register == null) {
                        register = findMethod(registry.getClass(), "register", String.class, Object.class);
                    }
                    if (register != null) {
                        register.setAccessible(true);
                        register.invoke(registry, fileName, ezConfigProxy);
                    }
                    return null;
                }
                return null;
            });

            return provider;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean wasSaved(Object providerProxy) {
        synchronized (SAVED) {
            AtomicBoolean b = SAVED.get(providerProxy);
            return b != null && b.get();
        }
    }

    private static Method findMethod(Class<?> cls, String name, Class<?>... params) {
        try {
            return cls.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            // try declared
            try { return cls.getDeclaredMethod(name, params); } catch (NoSuchMethodException ex) { return null; }
        }
    }

    private static Object defaultReturn(Class<?> ret) {
        if (!ret.isPrimitive()) return null;
        if (ret == boolean.class) return false;
        if (ret == byte.class) return (byte)0;
        if (ret == short.class) return (short)0;
        if (ret == int.class) return 0;
        if (ret == long.class) return 0L;
        if (ret == float.class) return 0f;
        if (ret == double.class) return 0d;
        if (ret == char.class) return '\0';
        return null;
    }
}
