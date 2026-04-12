package com.skyblockexp.ezframework.config;

/**
 * Platform modules implement this interface to provide config files into the
 * framework's {@link ConfigRegistry}. Implementations should register themselves via
 * {@code META-INF/services} so the core can discover them via {@link java.util.ServiceLoader}.
 *
 * <p>The plugin parameter is typed as {@link Object} so this interface can be
 * implemented for any server platform without importing Bukkit/Velocity/BungeeCord
 * classes. Implementations should cast to their expected type (e.g. {@code JavaPlugin}).
 */
public interface PlatformConfigProvider {

    /**
     * Provide config instances for the given plugin into the registry.
     *
     * @param plugin   host plugin instance (platform-specific; cast as needed)
     * @param registry config registry to populate
     * @throws Exception if config loading fails
     */
    void provide(Object plugin, ConfigRegistry registry) throws Exception;
}
