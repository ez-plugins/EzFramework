package com.skyblockexp.ezframework.storage.migration;

import com.skyblockexp.ezframework.EzPlugin;
import com.skyblockexp.ezframework.storage.StorageProvider;
import com.skyblockexp.ezframework.storage.StorageRegistry;
import com.skyblockexp.ezframework.storage.sql.JdbcStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MigrationManagerTest {

    static class PluginWithResources extends EzPlugin {
        private final Map<String, byte[]> resources = new HashMap<>();
        private final File dataFolder;

        PluginWithResources(File dataFolder) { this.dataFolder = dataFolder; }

        void addResource(String path, String content) { resources.put(path, content.getBytes(StandardCharsets.UTF_8)); }

        public InputStream getResource(String filename) {
            byte[] b = resources.get(filename);
            return b == null ? null : new ByteArrayInputStream(b);
        }

        public File getDataFolder() { return dataFolder; }

        @Override
        protected java.util.List<com.skyblockexp.ezframework.bootstrap.Component> components() { return Collections.emptyList(); }
    }

    // simple provider that records executed statements and supports MigrationCapable
    static class RecordingMigrationProvider implements StorageProvider, MigrationCapable {
        final List<List<String>> executed = new ArrayList<>();

        @Override public String name() { return "rec"; }
        @Override public void init(Object plugin) {}
        @Override public void close() {}
        @Override public void save(String path, java.util.Map<String, Object> data) {}
        @Override public Optional<java.util.Map<String, Object>> load(String path) { return Optional.empty(); }
        @Override public void delete(String path) {}
        @Override public boolean exists(String path) { return false; }

        @Override public void executeSqlStatements(java.util.List<String> statements) { executed.add(new ArrayList<>(statements)); }
        @Override public void executeSql(String sql) { executed.add(Collections.singletonList(sql)); }
    }

    // fake JDBC-backed provider that stores migration rows in-memory
    static class InMemoryJdbcMigrationProvider implements StorageProvider, MigrationCapable, JdbcStorage {
        final Map<String, String> migrations = new LinkedHashMap<>();
        final List<List<String>> executed = new ArrayList<>();

        @Override public String name() { return "jdbcprov"; }
        @Override public void init(Object plugin) {}
        @Override public void close() {}
        @Override public void save(String path, java.util.Map<String, Object> data) {}
        @Override public Optional<java.util.Map<String, Object>> load(String path) { return Optional.empty(); }
        @Override public void delete(String path) {}
        @Override public boolean exists(String path) { return false; }

        @Override public void executeSqlStatements(java.util.List<String> statements) { executed.add(new ArrayList<>(statements)); }
        @Override public void executeSql(String sql) { executed.add(Collections.singletonList(sql)); }

        @Override public java.util.List<java.util.Map<String, Object>> query(String sql, java.util.List<Object> params) {
            // SELECT id, checksum FROM ezframework_migrations WHERE provider = ? ORDER BY applied_at
            if (sql != null && sql.toLowerCase().contains("from ezframework_migrations")) {
                List<java.util.Map<String, Object>> out = new ArrayList<>();
                for (Map.Entry<String, String> e : migrations.entrySet()) {
                    Map<String, Object> row = new HashMap<>(); row.put("id", e.getKey()); row.put("checksum", e.getValue()); out.add(row);
                }
                return out;
            }
            // SELECT id FROM ezframework_migrations ... LIMIT 1
            if (sql != null && sql.toLowerCase().contains("select id from ezframework_migrations")) {
                List<java.util.Map<String, Object>> out = new ArrayList<>();
                for (String id : migrations.keySet()) { Map<String, Object> r = new HashMap<>(); r.put("id", id); out.add(r); break; }
                return out;
            }
            return Collections.emptyList();
        }

        @Override public int executeUpdate(String sql, java.util.List<Object> params) {
            String s = sql == null ? "" : sql.toLowerCase();
            if (s.contains("create table if not exists ezframework_migrations")) return 0;
            if (s.startsWith("insert into ezframework_migrations")) {
                if (params != null && params.size() >= 3) migrations.put(params.get(0).toString(), params.get(2) == null ? "" : params.get(2).toString());
                return 1;
            }
            if (s.startsWith("delete from ezframework_migrations")) {
                if (params != null && params.size() >= 1) migrations.remove(params.get(0).toString());
                return 1;
            }
            return 0;
        }
    }

    private File tmpDir;

    @BeforeEach
    public void before() throws Exception {
        tmpDir = new File(System.getProperty("java.io.tmpdir"), "migtest_" + UUID.randomUUID().toString());
        tmpDir.mkdirs();
        // clear registry via reflection (getAll() returns unmodifiable map)
        try {
            java.lang.reflect.Field f = com.skyblockexp.ezframework.storage.StorageRegistry.class.getDeclaredField("providers");
            f.setAccessible(true);
            java.util.Map<?,?> m = (java.util.Map<?,?>) f.get(null);
            m.clear();
        } catch (Exception ignored) {}
    }

    @AfterEach
    public void after() throws Exception {
        // cleanup
        if (tmpDir != null && tmpDir.exists()) {
            for (File f : Objects.requireNonNull(tmpDir.listFiles())) f.delete();
            tmpDir.delete();
        }
        StorageRegistry.closeAll();
    }

    @Test
    public void applyMigrations_writesManifest_and_executesSql_onMigrationCapableProvider() {
        PluginWithResources plugin = new PluginWithResources(tmpDir);
        plugin.addResource("migrations/index.txt", "001_init.sql\n");
        plugin.addResource("migrations/001_init.sql", "CREATE TABLE x (id VARCHAR(10));");

        RecordingMigrationProvider prov = new RecordingMigrationProvider();
        StorageRegistry.register(prov);

        MigrationManager mgr = new MigrationManager();
        mgr.applyMigrations(plugin);

        // provider should have executed the split statements
        assertFalse(prov.executed.isEmpty());
        // manifest file should exist and contain entry
        File manifest = new File(plugin.getDataFolder(), "applied_migrations.txt");
        assertTrue(manifest.exists());
        boolean found = false;
        try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(manifest))) {
            String l;
            while ((l = r.readLine()) != null) if (l.startsWith("001_init")) found = true;
        } catch (Exception ignored) {}
        assertTrue(found);
    }

    @Test
    public void rollbackLast_dbBacked_removesDbEntry_and_executesDownSql() throws Exception {
        // prepare plugin with down migration resource
        PluginWithResources plugin = new PluginWithResources(tmpDir);
        String id = "002_add";
        plugin.addResource("migrations/" + id + ".down.sql", "DROP TABLE if exists x;");

        InMemoryJdbcMigrationProvider prov = new InMemoryJdbcMigrationProvider();
        // pre-insert a migration row to simulate applied migrations
        prov.migrations.put(id, "deadbeef");
        StorageRegistry.register(prov);

        MigrationManager mgr = new MigrationManager();
        // rollback should find latest migration and execute down SQL
        boolean ok = mgr.rollbackLast(plugin, prov.name());
        assertTrue(ok);
        // provider should have executed the DOWN statements
        assertFalse(prov.executed.isEmpty());
        // migration entry removed from provider store
        assertFalse(prov.migrations.containsKey(id));
    }
}
