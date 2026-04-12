package com.skyblockexp.ezframework.storage.migration;

/**
 * Represents an executable migration. Implementations may be SQL-based or Java-based.
 */
public interface Migration {
    /**
     * Return the unique migration id.
     *
     * @return migration id (unique)
     */
    String id();

    /**
     * Return a human-readable description of the migration.
     *
     * @return human-readable description
     */
    String description();

    /**
     * Return the migration type (SQL or JAVA).
     *
     * @return migration type
     */
    MigrationType type();

    /**
     * Execute the migration.
     * @param ctx migration context
     * @throws Exception on failure
     */
    void apply(MigrationContext ctx) throws Exception;

    /**
     * Optional rollback; by default not supported.
     * @param ctx migration context
     * @throws Exception when rollback fails
     */
    default void rollback(MigrationContext ctx) throws Exception {
        throw new UnsupportedOperationException("rollback not supported");
    }

    /**
     * Optional target provider name. Return null for global migrations.
     * @return provider name or null
     */
    default String provider() { return null; }
}
