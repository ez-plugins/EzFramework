package com.skyblockexp.ezframework.storage.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Lightweight schema builder for SQL migrations. Produces CREATE TABLE SQL
 * compatible with MySQL (simple subset).
 */
public final class Schema {
    private final String tableName;
    private final List<Column> columns = new ArrayList<>();

    private Schema(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Create a new Schema builder for the given table name.
     *
     * @param tableName target table name
     * @return schema builder
     */
    public static Schema table(String tableName) {
        return new Schema(tableName);
    }
    /**
     * Add a default `id` primary key column.
     *
     * @return this schema builder
     */
    public Schema id() { columns.add(new Column("id", "VARCHAR(255)", true, true, false, null)); return this; }

    /**
     * Add a VARCHAR column.
     *
     * @param name column name
     * @param length varchar length
     * @param notNull whether the column is NOT NULL
     * @return this schema builder
     */
    public Schema string(String name, int length, boolean notNull) {
        columns.add(new Column(name, "VARCHAR(" + length + ")", notNull, false, false, null));
        return this;
    }

    /**
     * Add an INT column.
     *
     * @param name column name
     * @param notNull whether the column is NOT NULL
     * @return this schema builder
     */
    public Schema integer(String name, boolean notNull) {
        columns.add(new Column(name, "INT", notNull, false, false, null));
        return this;
    }

    /**
     * Add a TEXT column.
     *
     * @param name column name
     * @param notNull whether the column is NOT NULL
     * @return this schema builder
     */
    public Schema text(String name, boolean notNull) {
        columns.add(new Column(name, "TEXT", notNull, false, false, null));
        return this;
    }

    /**
     * Add a binary LONGBLOB column.
     *
     * @param name column name
     * @return this schema builder
     */
    public Schema binary(String name) { columns.add(new Column(name, "LONGBLOB", false, false, false, null)); return this; }

    /**
     * Add a boolean-like TINYINT(1) column.
     *
     * @param name column name
     * @param notNull whether the column is NOT NULL
     * @return this schema builder
     */
    public Schema bool(String name, boolean notNull) { columns.add(new Column(name, "TINYINT(1)", notNull, false, false, null)); return this; }

    /**
     * Build a CREATE TABLE SQL statement using the default dialect (MySQL).
     *
     * @return CREATE TABLE SQL
     */
    public String toCreateSql() {
        return toCreateSql(Dialect.MYSQL);
    }

    /** Dialect selection for generated SQL. */
    public enum Dialect {
        /** MySQL dialect. */
        MYSQL,
        /** H2 in-memory/embedded dialect. */
        H2
    }

    /**
     * Build CREATE TABLE SQL for the given dialect.
     *
     * @param dialect target SQL dialect
     * @return CREATE TABLE statement
     */
    public String toCreateSql(Dialect dialect) {
        StringJoiner join = new StringJoiner(", ");
        String pk = null;
        String quote = (dialect == Dialect.MYSQL) ? "`" : "";
        for (Column c : columns) {
            StringBuilder sb = new StringBuilder();
            sb.append(quote).append(c.name).append(quote).append(" ").append(c.type);
            if (c.notNull) sb.append(" NOT NULL");
            if (c.autoIncrement) sb.append(" AUTO_INCREMENT");
            if (c.defaultValue != null) sb.append(" DEFAULT '" + c.defaultValue + "'");
            join.add(sb.toString());
            if (c.primaryKey) pk = c.name;
        }
        String pkClause = (pk == null) ? "" : ", PRIMARY KEY (" + quote + pk + quote + ")";
        String suffix = (dialect == Dialect.MYSQL) ? " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;" : ";";
        return String.format("CREATE TABLE IF NOT EXISTS %s (%s%s)%s", quote + tableName + quote, join.toString(), pkClause, suffix);
    }

    private static final class Column {
        final String name;
        final String type;
        final boolean notNull;
        final boolean primaryKey;
        final boolean autoIncrement;
        final String defaultValue;

        Column(String name, String type, boolean notNull, boolean primaryKey, boolean autoIncrement, String defaultValue) {
            this.name = name; this.type = type; this.notNull = notNull; this.primaryKey = primaryKey; this.autoIncrement = autoIncrement; this.defaultValue = defaultValue;
        }
    }
}
