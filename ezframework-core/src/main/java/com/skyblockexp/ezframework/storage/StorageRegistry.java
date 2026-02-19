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

    public static void register(StorageProvider provider) {
        Objects.requireNonNull(provider, "provider");
        providers.put(provider.name(), provider);
    }

    public static StorageProvider get(String name) {
        return providers.get(name);
    }

    public static Map<String, StorageProvider> getAll() {
        return Collections.unmodifiableMap(providers);
    }

    public static void initAll(JavaPlugin plugin) {
        for (StorageProvider p : providers.values()) {
            try {
                p.init(plugin);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to init provider " + p.name() + ": " + e.getMessage());
            }
        }
    }

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
