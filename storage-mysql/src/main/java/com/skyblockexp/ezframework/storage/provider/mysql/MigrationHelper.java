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

    public MigrationHelper(DataSource ds) {
        this(ds, "ezframework_migrations");
    }

    public MigrationHelper(DataSource ds, String migrationsTable) {
        this.ds = ds;
        this.migrationsTable = migrationsTable;
    }

    public void ensureMigrationsTable() throws Exception {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `" + migrationsTable + "` (id VARCHAR(100) PRIMARY KEY, applied_at TIMESTAMP)");
        }
    }

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

    public static final class Migration {
        public final String id;
        public final List<String> statements;

        public Migration(String id, List<String> statements) {
            this.id = id;
            this.statements = statements;
        }
    }
}
