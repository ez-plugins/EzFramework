package com.skyblockexp.ezframework.config;

/**
 * Contract for a configuration migration.
 */
public interface ConfigMigration {
    /** Unique id for this migration. */
    String id();

    /** Apply the migration against the provided config. Implementations must be idempotent. */
    void apply(EzConfig config) throws Exception;
}
