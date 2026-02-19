package com.skyblockexp.ezframework;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-plugin registry for storing managers and shared objects.
 *
 * Use {@link #forPlugin(JavaPlugin)} to obtain the registry for a specific
 * plugin instance. This avoids cross-plugin collisions when multiple plugins
 * on the same JVM use EzFramework.
 */
public final class Registry {
    private static final Map<JavaPlugin, Registry> INSTANCES = new ConcurrentHashMap<>();

    private final JavaPlugin plugin;
    private final Map<String, Object> managers = new ConcurrentHashMap<>();

    private Registry(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * Return (or create) the registry associated with the given plugin.
     */
    public static Registry forPlugin(JavaPlugin plugin) {
        return INSTANCES.computeIfAbsent(plugin, Registry::new);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void register(String key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        managers.put(key, value);
    }

    public <T> void register(Class<T> key, T value) {
        Objects.requireNonNull(key, "key");
        register(key.getName(), value);
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(managers.get(key));
    }

    public <T> T get(String key, Class<T> clazz) {
        Object o = managers.get(key);
        if (o == null) return null;
        return clazz.cast(o);
    }

    public <T> T get(Class<T> key) {
        Object o = managers.get(key.getName());
        if (o == null) return null;
        return key.cast(o);
    }

    public Map<String, Object> getAll() {
        return java.util.Collections.unmodifiableMap(managers);
    }

    public void initAll() {
        for (Object o : managers.values()) {
            if (o == null) continue;
            try {
                if (o instanceof Manager) {
                    ((Manager) o).init();
                    continue;
                }
                tryInvokeNoArg(o, "init", "load");
            } catch (Exception ignored) {}
        }
    }

    public void shutdownAll() {
        for (Object o : managers.values()) {
            if (o == null) continue;
            try {
                if (o instanceof Manager) {
                    ((Manager) o).shutdown();
                    continue;
                }
                tryInvokeNoArg(o, "shutdown", "stop", "close");
            } catch (Exception ignored) {}
        }
    }

    private void tryInvokeNoArg(Object o, String... names) {
        for (String n : names) {
            try {
                Method m = o.getClass().getMethod(n);
                if (m != null) {
                    m.setAccessible(true);
                    m.invoke(o);
                    return;
                }
            } catch (NoSuchMethodException e) {
                // try next
            } catch (Exception ignored) {
                return;
            }
        }
    }
}
