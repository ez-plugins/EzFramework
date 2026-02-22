package com.skyblockexp.ezframework.storage.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry to map repository prefixes to SQL table and column definitions.
 * This allows migrations and repositories to agree on table/column layout.
 */
public final class ModelTableRegistry {
    private static final Map<String, TableMeta> MAP = new HashMap<>();

    private ModelTableRegistry() {}

    /**
     * Register table metadata for a repository prefix.
     *
     * @param prefix table prefix (repository prefix)
     * @param tableName SQL table name
     * @param columns map of column name -> SQL type
     */
    public static void register(String prefix, String tableName, Map<String, String> columns) {
        MAP.put(prefix, new TableMeta(tableName, new HashMap<>(columns)));
    }

    /**
     * Get registered table metadata for a prefix.
     *
     * @param prefix repository prefix
     * @return table metadata or null if not registered
     */
    public static TableMeta get(String prefix) { return MAP.get(prefix); }

    /**
     * Get an immutable view of all registered table metadata.
     *
     * @return map of prefix -> TableMeta
     */
    public static Map<String, TableMeta> all() { return Collections.unmodifiableMap(MAP); }

    /**
     * Metadata for a SQL table used by a repository.
     */
    public static final class TableMeta {
        private final String tableName;
        private final Map<String, String> columns; // name -> sql type

        TableMeta(String tableName, Map<String, String> columns) { this.tableName = tableName; this.columns = columns; }

        /**
         * Get the SQL table name.
         *
         * @return table name
         */
        public String tableName() { return tableName; }

        /**
         * Get the immutable column map.
         *
         * @return column name -> SQL type map
         */
        public Map<String, String> columns() { return Collections.unmodifiableMap(columns); }
    }
}
