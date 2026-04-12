package com.skyblockexp.ezframework.plugin.spi;

import java.util.Map;

/**
 * Service Provider Interface that every EzFramework module must implement.
 *
 * <p>Module JARs downloaded and loaded by {@code ezframework-plugin} are
 * expected to expose a class whose fully-qualified name is declared in their
 * {@code com.skyblockexp.ezframework.plugin.spi.EzModule}
 * file.  The manager plugin discovers and calls {@link #initialize} on each
 * implementation after the module JAR has been loaded.
 *
 * <p>The {@code plugin} parameter is the platform-specific host-plugin
 * instance (e.g. a Bukkit {@code JavaPlugin}, a Velocity {@code Plugin}
 * proxy, or a BungeeCord {@code Plugin}).  Modules must cast it to the
 * correct type for their platform.
 */
public interface EzModule {

    /**
     * Initialize this module.
     *
     * @param plugin    the host-plugin instance on the current platform
     * @param overrides any per-plugin configuration overrides declared in the
     *                  plugin's {@code ezframework.yml}
     * @throws Exception if initialization fails
     */
    void initialize(Object plugin, Map<String, String> overrides) throws Exception;

    /**
     * Called when the manager plugin is about to unload this module, giving it
     * a chance to release resources.
     *
     * @throws Exception if shutdown fails
     */
    default void shutdown() throws Exception {}
}
