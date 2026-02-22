package com.skyblockexp.ezframework.storage;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for storage providers allowing lookup by name. Providers should be
 * initialized by the host plugin (or a bootstrap component) before being used.
 */
public final class StorageRegistry {
    private static final Map<String, StorageProvider> providers = new ConcurrentHashMap<>();

    private StorageRegistry() {}

    /**
     * Register a {@link StorageProvider} under its {@link StorageProvider#name()}.
     *
     * @param provider provider to register (must not be null)
     */
    public static void register(StorageProvider provider) {
        Objects.requireNonNull(provider, "provider");
        providers.put(provider.name(), provider);
    }

    /**
     * Lookup a registered provider by name.
     *
     * @param name provider name
     * @return provider instance or null if not registered
     */
    public static StorageProvider get(String name) {
        return providers.get(name);
    }

    /**
     * Get an unmodifiable view of all registered providers.
     *
     * @return map of provider name -> provider
     */
    public static Map<String, StorageProvider> getAll() {
        return Collections.unmodifiableMap(providers);
    }

    /**
     * Initialize all registered providers with the given plugin context. This
     * performs best-effort initialization and logs failures per-provider.
     *
     * @param plugin plugin instance passed to provider init
     */
    public static void initAll(JavaPlugin plugin) {
        for (StorageProvider p : providers.values()) {
            try {
                p.init(plugin);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to init provider " + p.name() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Close all registered providers (best-effort).
     */
    public static void closeAll() {
        for (StorageProvider p : providers.values()) {
            try {
                p.close();
            } catch (Exception e) {
                // best-effort
            }
        }
    }
}
