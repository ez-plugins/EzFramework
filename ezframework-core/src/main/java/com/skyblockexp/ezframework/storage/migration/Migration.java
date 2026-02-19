package com.skyblockexp.ezframework.storage.migration;

/**
 * Represents an executable migration. Implementations may be SQL-based or Java-based.
 */
public interface Migration {
    String id();

    String description();

    MigrationType type();

    void apply(MigrationContext ctx) throws Exception;

    /**
     * Optional rollback; by default not supported.
     */
    default void rollback(MigrationContext ctx) throws Exception {
        throw new UnsupportedOperationException("rollback not supported");
    }

    /**
     * Optional target provider name. Return null for global migrations.
     */
    default String provider() { return null; }
}
