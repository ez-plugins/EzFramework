package com.skyblockexp.ezframework.storage.provider.mysql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

/**
 * Simple idempotent migration runner for plugin authors.
 *
 * Usage:
 * MigrationHelper mh = new MigrationHelper(ds);
 * mh.applyMigrations(List.of(new MigrationHelper.Migration("001-create-table", List.of("CREATE TABLE ..."))));
 */
public class MigrationHelper {
    private final DataSource ds;
    private final String migrationsTable;
    /**
     * Create a MigrationHelper that records applied migrations in the default
     * table `ezframework_migrations`.
     *
     * @param ds the DataSource to use for applying migrations
     */
    public MigrationHelper(DataSource ds) {
        this(ds, "ezframework_migrations");
    }

    /**
     * Create a MigrationHelper using a custom migrations table name.
     *
     * @param ds the DataSource to use
     * @param migrationsTable the table name to record applied migrations
     */
    public MigrationHelper(DataSource ds, String migrationsTable) {
        this.ds = ds;
        this.migrationsTable = migrationsTable;
    }

    /**
     * Ensure the migrations tracking table exists.
     *
     * @throws Exception on SQL errors
     */
    public void ensureMigrationsTable() throws Exception {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `" + migrationsTable + "` (id VARCHAR(100) PRIMARY KEY, applied_at TIMESTAMP)");
        }
    }

    /**
     * Apply the provided migrations in order if they have not already been
     * recorded as applied. This method is idempotent.
     *
     * @param migrations list of migrations to apply
     * @throws Exception on SQL errors
     */
    public void applyMigrations(List<Migration> migrations) throws Exception {
        ensureMigrationsTable();
        try (Connection c = ds.getConnection()) {
            boolean origAuto = c.getAutoCommit();
            try {
                c.setAutoCommit(false);
                for (Migration m : migrations) {
                    if (isApplied(c, m.id)) continue;
                    try (Statement st = c.createStatement()) {
                        for (String s : m.statements) st.executeUpdate(s);
                    }
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO `" + migrationsTable + "` (id, applied_at) VALUES (?, ?)") ) {
                        ps.setString(1, m.id);
                        ps.setObject(2, Instant.now());
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception ex) {
                try { c.rollback(); } catch (Exception ignored) {}
                throw ex;
            } finally {
                try { c.setAutoCommit(origAuto); } catch (Exception ignored) {}
            }
        }
    }

    private boolean isApplied(Connection c, String id) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM `" + migrationsTable + "` WHERE id = ? LIMIT 1")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Simple migration descriptor used by {@link MigrationHelper}.
     * Holds a stable migration id and the SQL statements to execute.
     */
    public static final class Migration {
        /** Unique migration identifier (e.g. '001-create-table'). */
        public final String id;

        /** SQL statements to execute for this migration. */
        public final List<String> statements;

        /**
         * Create a migration descriptor.
         *
         * @param id migration id
         * @param statements SQL statements to execute
         */
        public Migration(String id, List<String> statements) {
            this.id = id;
            this.statements = statements;
        }
    }
}
