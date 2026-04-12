package com.skyblockexp.ezframework.module;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Declares which EzFramework modules a plugin requires at runtime, and
 * provides optional per-plugin configuration overrides.
 *
 * <p>Plugin developers create an {@code ezframework.yml} in their plugin JAR
 * resources directory. The runtime loader ({@code ezframework-plugin}) reads
 * all installed plugins' descriptors, resolves the union of required modules,
 * and downloads any that are missing from GitHub releases.
 *
 * <h3>Example {@code ezframework.yml}</h3>
 * <pre>{@code
 * ezframework:
 *   modules:
 *     - core
 *     - config
 *     - mysql
 *   overrides:
 *     storage.backend: mysql
 *     config.path: custom/config
 * }</pre>
 */
public final class ModuleDescriptor {

    private final String pluginName;
    private final List<String> modules;
    private final Map<String, String> overrides;

    /**
     * Construct a module descriptor.
     *
     * @param pluginName human-readable plugin name (used in log messages)
     * @param modules    list of required EzFramework module IDs (e.g. {@code "core"}, {@code "mysql"})
     * @param overrides  per-plugin configuration overrides; may be empty
     */
    public ModuleDescriptor(String pluginName, List<String> modules, Map<String, String> overrides) {
        this.pluginName = Objects.requireNonNull(pluginName, "pluginName");
        this.modules = List.copyOf(Objects.requireNonNull(modules, "modules"));
        this.overrides = Map.copyOf(Objects.requireNonNull(overrides, "overrides"));
    }

    /**
     * The name of the plugin this descriptor belongs to.
     *
     * @return plugin name
     */
    public String pluginName() {
        return pluginName;
    }

    /**
     * The list of EzFramework module IDs this plugin requires.
     *
     * @return unmodifiable list of module IDs
     */
    public List<String> modules() {
        return modules;
    }

    /**
     * Per-plugin configuration overrides. Keys and values are plain strings;
     * the runtime loader applies them on top of global config for this plugin.
     *
     * @return unmodifiable map of override keys to override values
     */
    public Map<String, String> overrides() {
        return overrides;
    }

    @Override
    public String toString() {
        return "ModuleDescriptor{plugin=" + pluginName + ", modules=" + modules + "}";
    }
}
