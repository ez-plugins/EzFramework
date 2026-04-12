package com.skyblockexp.ezframework.storage.migration;

import com.skyblockexp.ezframework.storage.StorageProvider;
import com.skyblockexp.ezframework.storage.sql.JdbcStorage;

import java.util.List;
import java.util.Optional;

/**
 * Context passed to migrations when applied.
 *
 * <p>The host plugin is typed as {@link Object} so this class can be used
 * from any server platform without importing Bukkit/Velocity/BungeeCord APIs.
 * Implementations that need platform-specific behavior should cast the plugin
 * instance to their expected type.
 */
public final class MigrationContext {
    private final Object plugin;
    private final StorageProvider provider;

    /**
     * Create a migration context.
     *
     * @param plugin   host plugin (platform-specific; cast as needed)
     * @param provider storage provider that will receive migration statements
     */
    public MigrationContext(Object plugin, StorageProvider provider) {
        this.plugin = plugin;
        this.provider = provider;
    }

    /**
     * Get the host plugin for this migration context.
     *
     * @return host plugin instance
     */
    public Object plugin() {
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
     * If the underlying provider implements {@link MigrationCapable} this returns it;
     * otherwise an empty Optional.
     *
     * @return optional MigrationCapable instance
     */
    public Optional<MigrationCapable> migrationCapable() {
        return provider instanceof MigrationCapable
                ? Optional.of((MigrationCapable) provider)
                : Optional.empty();
    }

    /**
     * If the underlying provider implements {@link JdbcStorage} this returns it;
     * otherwise an empty Optional.
     *
     * @return optional JdbcStorage instance
     */
    public Optional<JdbcStorage> jdbc() {
        return provider instanceof JdbcStorage
                ? Optional.of((JdbcStorage) provider)
                : Optional.empty();
    }

    /**
     * Convenience: execute a list of SQL statements against the provider if it
     * supports migrations.
     *
     * @param statements SQL statements to execute
     * @throws Exception when execution fails or provider does not support statements
     */
    public void executeSqlStatements(List<String> statements) throws Exception {
        if (provider instanceof MigrationCapable) {
            ((MigrationCapable) provider).executeSqlStatements(statements);
            return;
        }
        throw new UnsupportedOperationException("Provider does not support executeSqlStatements");
    }
}
