package com.skyblockexp.ezframework.storage.migration;

/**
 * Capability interface for providers able to execute SQL or accept migration application.
 */
public interface MigrationCapable {
    void executeSql(String sql) throws Exception;

    /**
     * Execute a list of SQL statements. Default implementation executes each
     * statement separately via `executeSql`. Providers may override to run
     * the statements inside a transaction.
     */
    default void executeSqlStatements(java.util.List<String> statements) throws Exception {
        for (String s : statements) executeSql(s);
    }
}
