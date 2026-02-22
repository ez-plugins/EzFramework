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
     *
     * @param plugin plugin instance to obtain the registry for
     * @return registry instance for the given plugin
     */
    public static Registry forPlugin(JavaPlugin plugin) {
        return INSTANCES.computeIfAbsent(plugin, Registry::new);
    }

    /**
     * Obtain the Bukkit plugin instance backing this registry.
     *
     * @return the associated {@link JavaPlugin}
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Register an object in this registry under a string key.
     *
     * @param key   the unique key to register the value under
     * @param value the value to store; must not be null
     */
    public void register(String key, Object value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        managers.put(key, value);
    }

    /**
     * Register an object in this registry using its class as the key.
     * The registration key will be the class' fully-qualified name.
     *
     * @param key   the class to use as the registration key
     * @param value the value to store; must not be null
     * @param <T>   the type of the value
     */
    public <T> void register(Class<T> key, T value) {
        Objects.requireNonNull(key, "key");
        register(key.getName(), value);
    }

    /**
     * Retrieve a value stored under the given string key.
     *
     * @param key registration key
     * @return an {@link Optional} containing the value if present
     */
    public Optional<Object> get(String key) {
        return Optional.ofNullable(managers.get(key));
    }

    /**
     * Retrieve a typed value stored under the given string key.
     *
     * @param key   registration key
     * @param clazz expected class of the stored object
     * @param <T>   expected type
     * @return the stored value cast to {@code T}, or {@code null} if absent
     */
    public <T> T get(String key, Class<T> clazz) {
        Object o = managers.get(key);
        if (o == null) return null;
        return clazz.cast(o);
    }

    /**
     * Retrieve a typed value registered using its class as the key.
     *
     * @param key class used when registering the value
     * @param <T> expected type
     * @return the stored value cast to {@code T}, or {@code null} if absent
     */
    public <T> T get(Class<T> key) {
        Object o = managers.get(key.getName());
        if (o == null) return null;
        return key.cast(o);
    }

    /**
     * Return an unmodifiable view of all registered objects in this registry.
     *
     * @return unmodifiable map of registration key to object
     */
    public Map<String, Object> getAll() {
        return java.util.Collections.unmodifiableMap(managers);
    }

    /**
     * Invoke initialization on all registered manager objects.
     *
     * If a value implements {@link Manager} its {@code init()} method will be
     * called. Otherwise this will attempt to invoke a no-arg method named
     * {@code init} or {@code load} reflectively.
     */
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

    /**
     * Invoke shutdown on all registered manager objects.
     *
     * If a value implements {@link Manager} its {@code shutdown()} method will
     * be called. Otherwise this will attempt to invoke a no-arg method named
     * {@code shutdown}, {@code stop} or {@code close} reflectively.
     */
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
