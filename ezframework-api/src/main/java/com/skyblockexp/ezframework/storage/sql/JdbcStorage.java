package com.skyblockexp.ezframework.storage.sql;

import java.util.List;
import java.util.Map;

/**
 * Simple JDBC-like abstraction to allow repositories to issue queries and read
 * results. Implementations should provide basic mapping of resultset rows to
 * maps.
 */
public interface JdbcStorage {
    /**
     * Execute a SQL query with parameters and return rows as a list of column->value maps.
     * @param sql SQL statement
     * @param params positional parameters (may be null)
     * @return list of rows where each row is a map of column->value
     * @throws Exception on SQL or mapping errors
     */
    java.util.List<java.util.Map<String, Object>> query(String sql, java.util.List<Object> params) throws Exception;

    /**
     * Execute a SQL update/insert/delete with parameters.
     * @param sql SQL statement
     * @param params positional parameters (may be null)
     * @return number of affected rows
     * @throws Exception on SQL errors
     */
    int executeUpdate(String sql, java.util.List<Object> params) throws Exception;
}
