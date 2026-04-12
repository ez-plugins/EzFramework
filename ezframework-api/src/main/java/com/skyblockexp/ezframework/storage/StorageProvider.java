package com.skyblockexp.ezframework.storage;

import java.util.Map;
import java.util.Optional;

/**
 * Abstraction for storage backends (YML files, MySQL, SQLite, etc.).
 * Implementations provide simple key-based load/save/delete semantics where
 * the framework and repositories map domain IDs to storage paths.
 *
 * <p>The {@link #init(Object)} parameter is typed as {@link Object} so this
 * interface can be implemented for any server platform without a platform
 * dependency in the API module. Implementations should cast to their expected
 * platform plugin type (e.g. {@code JavaPlugin}, {@code PluginContainer}).
 */
public interface StorageProvider {
    /**
     * Human-friendly name for the provider.
     *
     * @return provider name
     */
    String name();

    /**
     * Initialize the provider with the host plugin. Called once during startup.
     *
     * @param plugin hosting plugin instance (platform-specific; cast as needed)
     * @throws Exception on initialization failure
     */
    void init(Object plugin) throws Exception;

    /**
     * Close / cleanup the provider (release connections, flush caches).
     *
     * @throws Exception on close failure
     */
    void close() throws Exception;

    /**
     * Save a flat map at the given path.
     *
     * @param path storage path
     * @param data flat map to save
     * @throws Exception on save failure
     */
    void save(String path, Map<String, Object> data) throws Exception;

    /**
     * Load a map previously saved at the given path.
     *
     * @param path storage path
     * @return optional map if present
     * @throws Exception on load failure
     */
    Optional<Map<String, Object>> load(String path) throws Exception;

    /**
     * Remove the entry at the given path.
     *
     * @param path storage path
     * @throws Exception on delete failure
     */
    void delete(String path) throws Exception;

    /**
     * Return whether an entry exists at the path.
     *
     * @param path storage path
     * @return true if entry exists
     * @throws Exception on existence check failure
     */
    boolean exists(String path) throws Exception;
}
