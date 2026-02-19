package com.skyblockexp.ezframework.storage.sql;

import java.util.List;
import java.util.Map;

/**
 * Simple JDBC-like abstraction to allow repositories to issue queries and read
 * results. Implementations should provide basic mapping of resultset rows to
 * maps.
 */
public interface JdbcStorage {
    /** Execute a SQL query with parameters and return rows as a list of column->value maps. */
    java.util.List<java.util.Map<String, Object>> query(String sql, java.util.List<Object> params) throws Exception;

    /** Execute a SQL update/insert/delete with parameters. Returns affected row count. */
    int executeUpdate(String sql, java.util.List<Object> params) throws Exception;
}
