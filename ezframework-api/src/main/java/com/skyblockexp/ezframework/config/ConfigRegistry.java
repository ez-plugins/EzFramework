package com.skyblockexp.ezframework.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for framework configs. Core components and platform
 * adapters can register EzConfig instances here for discovery and lifecycle.
 */
public class ConfigRegistry {
    private static volatile ConfigRegistry defaultRegistry;

    // Map: filename -> (ownerKey -> EzConfig)
    private final Map<String, Map<String, EzConfig>> configs = new ConcurrentHashMap<>();

    public void register(String name, EzConfig cfg) {
        String ownerKey = cfg.getDataFolder().toAbsolutePath().toString();
        configs.computeIfAbsent(name, k -> new ConcurrentHashMap<>()).put(ownerKey, cfg);
    }

    /**
     * Returns one matching config for the given name (legacy single-config API).
     * If multiple owners exist, an arbitrary one is returned. Prefer {@link #getAll(String)}.
     */
    public Optional<EzConfig> get(String name) {
        Map<String, EzConfig> m = configs.get(name);
        if (m == null || m.isEmpty()) return Optional.empty();
        return Optional.of(m.values().iterator().next());
    }

    /** Returns all configs registered under the given filename across owners. */
    public Collection<EzConfig> getAll(String name) {
        Map<String, EzConfig> m = configs.get(name);
        if (m == null) return Collections.emptyList();
        return Collections.unmodifiableCollection(m.values());
    }

    public Collection<EzConfig> list() {
        java.util.List<EzConfig> out = new java.util.ArrayList<>();
        for (Map<String, EzConfig> m : configs.values()) {
            out.addAll(m.values());
        }
        return Collections.unmodifiableCollection(out);
    }

    /** Unregister all configs for the given filename. */
    public void unregister(String name) {
        configs.remove(name);
    }

    /** Unregister a specific config instance for the given filename. */
    public void unregister(String name, EzConfig cfg) {
        Map<String, EzConfig> m = configs.get(name);
        if (m == null) return;
        String ownerKey = cfg.getDataFolder().toAbsolutePath().toString();
        m.remove(ownerKey);
        if (m.isEmpty()) configs.remove(name);
    }

    public void clear() {
        configs.clear();
    }

    public static void setDefault(ConfigRegistry r) {
        defaultRegistry = r;
    }

    public static ConfigRegistry getDefault() {
        return defaultRegistry;
    }
}
