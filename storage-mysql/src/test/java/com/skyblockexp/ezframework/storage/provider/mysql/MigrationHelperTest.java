package com.skyblockexp.ezframework.storage.provider.mysql;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MigrationHelperTest {

    private final String URL = "jdbc:h2:mem:mh;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @Test
    public void apply_migrations_idempotent() throws Exception {
        javax.sql.DataSource ds = new javax.sql.DataSource() {
            @Override public Connection getConnection() { try { return DriverManager.getConnection(URL); } catch (Exception e) { throw new RuntimeException(e); } }
            @Override public Connection getConnection(String username, String password) { try { return DriverManager.getConnection(URL, username, password); } catch (Exception e) { throw new RuntimeException(e); } }
            @Override public <T> T unwrap(Class<T> iface) { return null; }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
            @Override public java.io.PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(java.io.PrintWriter out) {}
            @Override public void setLoginTimeout(int seconds) {}
            @Override public int getLoginTimeout() { return 0; }
            @Override public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getGlobal(); }
        };

        MigrationHelper mh = new MigrationHelper(ds, "mh_mig");
        mh.ensureMigrationsTable();
        MigrationHelper.Migration m = new MigrationHelper.Migration("001_create", List.of("CREATE TABLE IF NOT EXISTS t_mh (id INT PRIMARY KEY)"));
        mh.applyMigrations(List.of(m));
        // re-apply should not fail and will skip already applied
        mh.applyMigrations(List.of(m));
    }
}
