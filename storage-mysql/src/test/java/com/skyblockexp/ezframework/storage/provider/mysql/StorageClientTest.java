package com.skyblockexp.ezframework.storage.provider.mysql;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class StorageClientTest {

    private final String URL = "jdbc:h2:mem:sc;DB_CLOSE_DELAY=-1;MODE=MySQL";

    @Test
    public void crud_flow_and_batch_query() throws Exception {
        StorageClient c = new StorageClient(dsFromUrl(URL));
        c.setTable("sc_test");
        c.ensureTable();

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Alice");
        payload.put("age", 30);

        c.save("p/1", payload);
        assertTrue(c.exists("p/1"));

        Optional<Map<String, Object>> loaded = c.load("p/1");
        assertTrue(loaded.isPresent());
        assertEquals("Alice", loaded.get().get("name"));

        // batch save
        Map<String, Map<String, Object>> batch = new HashMap<>();
        batch.put("p/2", Map.of("name","Bob"));
        batch.put("p/3", Map.of("name","Carol"));
        c.batchSave(batch);

        // simple query selecting path and data
        java.util.List<Map<String,Object>> rows = c.query("SELECT path, data FROM sc_test", null);
        assertTrue(rows.size() >= 3);

        c.delete("p/1");
        assertFalse(c.exists("p/1"));
    }

    private static DataSource dsFromConn(Connection c) {
        return new DataSource() {
            @Override public Connection getConnection() { return c; }
            @Override public Connection getConnection(String username, String password) { return c; }
            @Override public <T> T unwrap(Class<T> iface) { return null; }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
            @Override public java.io.PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(java.io.PrintWriter out) {}
            @Override public void setLoginTimeout(int seconds) {}
            @Override public int getLoginTimeout() { return 0; }
            @Override public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getGlobal(); }
        };
    }

    private static DataSource dsFromUrl(String url) {
        return new DataSource() {
            @Override public Connection getConnection() { try { return DriverManager.getConnection(url); } catch (Exception e) { throw new RuntimeException(e); } }
            @Override public Connection getConnection(String username, String password) { try { return DriverManager.getConnection(url, username, password); } catch (Exception e) { throw new RuntimeException(e); } }
            @Override public <T> T unwrap(Class<T> iface) { return null; }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
            @Override public java.io.PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(java.io.PrintWriter out) {}
            @Override public void setLoginTimeout(int seconds) {}
            @Override public int getLoginTimeout() { return 0; }
            @Override public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getGlobal(); }
        };
    }
}
