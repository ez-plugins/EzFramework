package com.skyblockexp.ezframework.storage;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;

/**
 * Abstraction for storage backends (YML files, MySQL, SQLite, etc.).
 * Implementations provide simple key-based load/save/delete semantics where
 * the framework and repositories map domain IDs to storage paths.
 */
public interface StorageProvider {
    /** Human-friendly name for the provider. */
    String name();

    /** Initialize the provider with the host plugin. Called once during startup. */
    void init(JavaPlugin plugin) throws Exception;

    /** Close / cleanup the provider (release connections, flush caches). */
    void close() throws Exception;

    /** Save a flat map at the given path. */
    void save(String path, Map<String, Object> data) throws Exception;

    /** Load a map previously saved at the given path. */
    Optional<Map<String, Object>> load(String path) throws Exception;

    /** Remove the entry at the given path. */
    void delete(String path) throws Exception;

    /** Return whether an entry exists at the path. */
    boolean exists(String path) throws Exception;
}
