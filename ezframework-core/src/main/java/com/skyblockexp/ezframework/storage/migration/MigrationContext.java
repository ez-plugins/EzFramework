package com.skyblockexp.ezframework.storage.migration;

import com.skyblockexp.ezframework.storage.StorageProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Context passed to migrations when applied.
 */
public final class MigrationContext {
    private final JavaPlugin plugin;
    private final StorageProvider provider;

    /**
     * Create a migration context.
     * @param plugin host plugin
     * @param provider storage provider that will receive migration statements
     */
    public MigrationContext(JavaPlugin plugin, StorageProvider provider) {
        this.plugin = plugin;
        this.provider = provider;
    }

    /**
     * Get the host plugin for this migration context.
     *
     * @return host plugin
     */
    public JavaPlugin plugin() {
        return plugin;
    }

    /**
     * Get the storage provider associated with this migration context.
     *
     * @return storage provider
     */
    public StorageProvider provider() {
        return provider;
    }

    /**
     * If the underlying provider implements {@link com.skyblockexp.ezframework.storage.migration.MigrationCapable}
     * this returns it; otherwise an empty Optional.
     */
    /**
     * If the underlying provider implements {@link com.skyblockexp.ezframework.storage.migration.MigrationCapable}
     * this returns it; otherwise an empty Optional.
     *
     * @return optional MigrationCapable instance
     */
    public java.util.Optional<com.skyblockexp.ezframework.storage.migration.MigrationCapable> migrationCapable() {
        return provider instanceof com.skyblockexp.ezframework.storage.migration.MigrationCapable ?
                java.util.Optional.of((com.skyblockexp.ezframework.storage.migration.MigrationCapable) provider) : java.util.Optional.empty();
    }

    /**
     * If the underlying provider implements {@link com.skyblockexp.ezframework.storage.sql.JdbcStorage}
     * this returns it; otherwise an empty Optional.
     */
    /**
     * If the underlying provider implements {@link com.skyblockexp.ezframework.storage.sql.JdbcStorage}
     * this returns it; otherwise an empty Optional.
     *
     * @return optional JdbcStorage instance
     */
    public java.util.Optional<com.skyblockexp.ezframework.storage.sql.JdbcStorage> jdbc() {
        return provider instanceof com.skyblockexp.ezframework.storage.sql.JdbcStorage ?
                java.util.Optional.of((com.skyblockexp.ezframework.storage.sql.JdbcStorage) provider) : java.util.Optional.empty();
    }

    /**
     * Convenience: execute a list of SQL statements against the provider if it supports migrations.
     * @param statements SQL statements to execute
     * @throws Exception when execution fails or when provider does not support statements
     */
    public void executeSqlStatements(java.util.List<String> statements) throws Exception {
        if (provider instanceof com.skyblockexp.ezframework.storage.migration.MigrationCapable) {
            ((com.skyblockexp.ezframework.storage.migration.MigrationCapable) provider).executeSqlStatements(statements);
            return;
        }
        throw new UnsupportedOperationException("Provider does not support executeSqlStatements");
    }
}
