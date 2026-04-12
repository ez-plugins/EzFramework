package com.skyblockexp.ezframework.storage.migration;

/**
 * Capability interface for providers able to execute SQL or accept migration application.
 */
public interface MigrationCapable {
    /**
     * Execute a single SQL statement.
     * @param sql SQL statement to execute
     * @throws Exception on failure
     */
    void executeSql(String sql) throws Exception;

    /**
     * Execute a list of SQL statements. Default implementation executes each
     * statement separately via {@link #executeSql(String)}. Providers may
     * override to run the statements inside a transaction.
     * @param statements list of SQL statements to execute
     * @throws Exception on failure
     */
    default void executeSqlStatements(java.util.List<String> statements) throws Exception {
        for (String s : statements) executeSql(s);
    }
}
