package com.skyblockexp.ezframework.storage.provider.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A typed helper around a `DataSource` that provides convenient get/save/delete/batch
 * operations. Stores payloads as JSON (uses Jackson) in a text column.
 */
public class StorageClient {
    private final DataSource ds;
    private final ObjectMapper mapper = new ObjectMapper();
    private String table = "ezframework_storage";

    public StorageClient(DataSource ds) {
        this.ds = ds;
    }

    public StorageClient setTable(String table) {
        if (table != null && !table.isEmpty()) this.table = table;
        return this;
    }

    public void ensureTable() throws Exception {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `" + table + "` (path VARCHAR(255) PRIMARY KEY, data MEDIUMTEXT, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
        }
    }

    public void save(String path, Map<String, Object> data) throws Exception {
        String json = mapper.writeValueAsString(data == null ? Collections.emptyMap() : data);
        String sql = "INSERT INTO `" + table + "` (path, data) VALUES (?, ?) ON DUPLICATE KEY UPDATE data = VALUES(data), updated_at = CURRENT_TIMESTAMP";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.setString(2, json);
            ps.executeUpdate();
        }
    }

    public Optional<Map<String, Object>> load(String path) throws Exception {
        String sql = "SELECT data FROM `" + table + "` WHERE path = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, path);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                String json = rs.getString(1);
                if (json == null) return Optional.empty();
                //noinspection unchecked
                return Optional.of(mapper.readValue(json, Map.class));
            }
        }
    }

    public void delete(String path) throws Exception {
        String sql = "DELETE FROM `" + table + "` WHERE path = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.executeUpdate();
        }
    }

    public boolean exists(String path) throws Exception {
        String sql = "SELECT 1 FROM `" + table + "` WHERE path = ? LIMIT 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, path);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void batchSave(Map<String, Map<String, Object>> entries) throws Exception {
        if (entries == null || entries.isEmpty()) return;
        String sql = "INSERT INTO `" + table + "` (path, data) VALUES (?, ?) ON DUPLICATE KEY UPDATE data = VALUES(data), updated_at = CURRENT_TIMESTAMP";
        try (Connection c = ds.getConnection()) {
            boolean origAuto = c.getAutoCommit();
            try {
                c.setAutoCommit(false);
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    for (Map.Entry<String, Map<String, Object>> e : entries.entrySet()) {
                        ps.setString(1, e.getKey());
                        ps.setString(2, mapper.writeValueAsString(e.getValue()));
                        ps.addBatch();
                    }
                    ps.executeBatch();
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

    public List<Map<String, Object>> query(String sql, List<Object> params) throws Exception {
        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                java.sql.ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        String name = md.getColumnLabel(i);
                        Object val = rs.getObject(i);
                        row.put(name == null ? null : name.toLowerCase(), val);
                    }
                    out.add(row);
                }
            }
        }
        return out;
    }

    // Async helpers using CompletableFuture - useful to avoid blocking main thread.
    public CompletableFuture<Void> saveAsync(String path, Map<String, Object> data) {
        return CompletableFuture.runAsync(() -> { try { save(path, data); } catch (Exception e) { throw new RuntimeException(e); } });
    }

    public CompletableFuture<Optional<Map<String, Object>>> loadAsync(String path) {
        return CompletableFuture.supplyAsync(() -> { try { return load(path); } catch (Exception e) { throw new RuntimeException(e); } });
    }

    public CompletableFuture<Void> deleteAsync(String path) {
        return CompletableFuture.runAsync(() -> { try { delete(path); } catch (Exception e) { throw new RuntimeException(e); } });
    }
}
