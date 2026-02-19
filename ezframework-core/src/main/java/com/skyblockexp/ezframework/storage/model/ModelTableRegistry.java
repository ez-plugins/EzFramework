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

    public static void register(String prefix, String tableName, Map<String, String> columns) {
        MAP.put(prefix, new TableMeta(tableName, new HashMap<>(columns)));
    }

    public static TableMeta get(String prefix) { return MAP.get(prefix); }

    public static Map<String, TableMeta> all() { return Collections.unmodifiableMap(MAP); }

    public static final class TableMeta {
        private final String tableName;
        private final Map<String, String> columns; // name -> sql type

        TableMeta(String tableName, Map<String, String> columns) { this.tableName = tableName; this.columns = columns; }

        public String tableName() { return tableName; }
        public Map<String, String> columns() { return Collections.unmodifiableMap(columns); }
    }
}
