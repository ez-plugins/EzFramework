package com.skyblockexp.ezframework.storage.migration;

import com.skyblockexp.ezframework.storage.StorageProvider;
import com.skyblockexp.ezframework.storage.StorageRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Migration manager. Discovers migrations and applies them. When a provider
 * supports JDBC (`JdbcStorage`) the manager will prefer to record applied
 * migrations in a provider-side table (`ezframework_migrations`) instead of
 * the plugin-local `applied_migrations.txt` manifest. This improves
 * reliability in multi-node deployments.
 */
public class MigrationManager {

    /**
     * Create a migration manager.
     */
    public MigrationManager() {
    }

    /**
     * Discover and apply migrations for all providers registered in {@link com.skyblockexp.ezframework.storage.StorageRegistry}.
     * @param plugin host plugin used to locate resources
     */
    public void applyMigrations(JavaPlugin plugin) {
        // discover providers
        for (Map.Entry<String, StorageProvider> e : StorageRegistry.getAll().entrySet()) {
            StorageProvider provider = e.getValue();
            if (provider == null) continue;
            if (provider instanceof MigrationCapable) {
                // provider supports migrations — discover resources in plugin jar under /migrations/
                try {
                    // Prefer provider-side migration recording when provider supports JDBC
                    boolean providerDbTracks = provider instanceof com.skyblockexp.ezframework.storage.sql.JdbcStorage;

                    List<MigrationDescriptor> migrations = discoverMigrations(plugin);
                    // also discover service-registered Java migrations
                    List<Migration> javaMigs = discoverServiceMigrations(plugin);

                    java.util.LinkedHashMap<String, String> applied;
                    if (providerDbTracks) {
                        applied = readAppliedFromDb((com.skyblockexp.ezframework.storage.sql.JdbcStorage) provider, provider.name());
                    } else {
                        File manifest = new File(pluginDataFolder(plugin), "applied_migrations.txt");
                        applied = readApplied(manifest);
                    }

                    // apply resource SQL migrations
                    for (MigrationDescriptor d : migrations) {
                        if (d.type() != MigrationType.SQL) continue;
                        if (d.providerName() != null && !d.providerName().equals(provider.name())) continue;
                        if (applied.containsKey(d.id())) {
                            String existing = applied.get(d.id());
                            InputStream chkIn = pluginResource(plugin, d.resourcePath());
                            String currentChecksum = "";
                            if (chkIn != null) {
                                currentChecksum = checksumFromBytes(SqlSplitter.split(new String(chkIn.readAllBytes(), StandardCharsets.UTF_8)).toString().getBytes(StandardCharsets.UTF_8));
                                try { chkIn.close(); } catch (Exception ignored) {}
                            }
                            if (!existing.equals(currentChecksum)) {
                                plugin.getLogger().severe("Checksum mismatch for applied migration " + d.id() + ". Manual review required; skipping to avoid data corruption.");
                                continue;
                            }
                            plugin.getLogger().fine("Skipping already applied migration: " + d.id());
                            continue;
                        }
                        plugin.getLogger().info("Applying migration " + d.id() + " -> " + d.resourcePath());
                        try (InputStream in = pluginResource(plugin, d.resourcePath())) {
                            if (in == null) {
                                plugin.getLogger().severe("Migration resource not found: " + d.resourcePath());
                                continue;
                            }
                            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                            java.util.List<String> stmts = SqlSplitter.split(sql);
                            ((MigrationCapable) provider).executeSqlStatements(stmts);
                            String checksum = checksum(sql.getBytes(StandardCharsets.UTF_8));
                            if (providerDbTracks) {
                                writeAppliedToDb((com.skyblockexp.ezframework.storage.sql.JdbcStorage) provider, d.id(), provider.name(), checksum);
                            } else {
                                File manifest = new File(pluginDataFolder(plugin), "applied_migrations.txt");
                                appendApplied(manifest, d.id(), provider.name(), checksum);
                            }
                            applied.put(d.id(), checksum);
                        }
                    }

                    // apply Java migrations discovered via service loader
                    for (Migration m : javaMigs) {
                        String target = m.provider();
                        if (target != null && !target.equals(provider.name())) continue;
                        if (applied.containsKey(m.id())) {
                            plugin.getLogger().fine("Skipping already applied Java migration: " + m.id());
                            continue;
                        }
                        plugin.getLogger().info("Applying Java migration: " + m.id());
                        m.apply(new MigrationContext(plugin, provider));
                        String cs = checksum(m.id().getBytes(StandardCharsets.UTF_8));
                        if (providerDbTracks) {
                            writeAppliedToDb((com.skyblockexp.ezframework.storage.sql.JdbcStorage) provider, m.id(), provider.name(), cs);
                        } else {
                            File manifest = new File(pluginDataFolder(plugin), "applied_migrations.txt");
                            appendApplied(manifest, m.id(), provider.name(), cs);
                        }
                        applied.put(m.id(), cs);
                    }
                } catch (Exception ex) {
                    plugin.getLogger().severe("Migration discovery/apply failed: " + ex.getMessage());
                }
            }
        }
    }

    private List<MigrationDescriptor> discoverMigrations(JavaPlugin plugin) throws Exception {
        List<MigrationDescriptor> list = new ArrayList<>();
        // Simple discovery: list resources under /migrations/ by attempting to load a known index resource.
        InputStream idx = pluginResource(plugin, "migrations/index.txt");
        if (idx == null) return list;
        try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(idx))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String providerName = null;
                String filename = line;
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    providerName = parts[0].trim();
                    filename = parts[1].trim();
                }
                MigrationType type = filename.endsWith(".sql") ? MigrationType.SQL : MigrationType.JAVA;
                list.add(new MigrationDescriptor(filename.replace('/', '_'), "from resources", type, "migrations/" + filename, providerName));
            }
        }
        return list;
    }

    private List<Migration> discoverServiceMigrations(JavaPlugin plugin) {
        List<Migration> out = new ArrayList<>();
        try (InputStream is = pluginResource(plugin, "META-INF/services/" + Migration.class.getName())) {
            if (is == null) return out;
            try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    try {
                        Class<?> cls = Class.forName(line, true, plugin.getClass().getClassLoader());
                        Object inst = cls.getDeclaredConstructor().newInstance();
                        if (inst instanceof Migration) out.add((Migration) inst);
                    } catch (Throwable t) {
                        plugin.getLogger().severe("Failed to load migration class: " + line + " -> " + t.getMessage());
                    }
                }
            }
        } catch (Exception ignored) {}
        return out;
    }

    /**
     * Obtain a plugin resource input stream in a robust way. Some runtimes
     * may not provide JavaPlugin.getResource(String); attempt to call it
     * reflectively and fall back to classloader-based lookup.
     */
    private InputStream pluginResource(JavaPlugin plugin, String path) {
        try {
            try {
                java.lang.reflect.Method m = plugin.getClass().getMethod("getResource", String.class);
                Object res = m.invoke(plugin, path);
                if (res instanceof InputStream) return (InputStream) res;
            } catch (NoSuchMethodException ignored) {
                // fall through to classloader lookup
            }
        } catch (Throwable ignored) {
        }

        // try classloader resource (no leading slash)
        InputStream is = plugin.getClass().getClassLoader().getResourceAsStream(path);
        if (is != null) return is;
        // try class-relative resource (leading slash)
        return plugin.getClass().getResourceAsStream('/' + path);
    }

    /**
     * Obtain a File representing the plugin data folder. If JavaPlugin doesn't
     * expose `getDataFolder()` at runtime, fall back to a temp directory.
     */
    private File pluginDataFolder(JavaPlugin plugin) {
        try {
            try {
                java.lang.reflect.Method m = plugin.getClass().getMethod("getDataFolder");
                Object res = m.invoke(plugin);
                if (res instanceof File) return (File) res;
            } catch (NoSuchMethodException ignored) {
                // fall through
            }
        } catch (Throwable ignored) {}

        File tmp = new File(System.getProperty("java.io.tmpdir"), plugin.getClass().getSimpleName());
        if (!tmp.exists()) tmp.mkdirs();
        return tmp;
    }


    /**
     * Read applied migrations into a LinkedHashMap preserving order: id -> checksum
     */
    private java.util.LinkedHashMap<String, String> readApplied(File manifest) {
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        try {
            if (!manifest.exists()) return map;
            try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(manifest))) {
                String l;
                while ((l = r.readLine()) != null) {
                    l = l.trim();
                    if (l.isEmpty()) continue;
                    String[] parts = l.split("\\|", 4);
                    // expected: id|timestamp|provider|checksum
                    if (parts.length >= 4) map.put(parts[0], parts[3]);
                    else if (parts.length >= 1) map.put(parts[0], "");
                }
            }
        } catch (Exception ignored) {}
        return map;
    }

    private void appendApplied(File manifest, String id, String provider, String checksum) {
        try {
            if (!manifest.getParentFile().exists()) manifest.getParentFile().mkdirs();
            try (java.io.FileWriter fw = new java.io.FileWriter(manifest, true)) {
                fw.write(id + "|" + System.currentTimeMillis() + "|" + (provider == null ? "" : provider) + "|" + checksum + "\n");
            }
        } catch (Exception ignored) {
        }
    }

    private void removeApplied(File manifest, String id) {
        try {
            if (!manifest.exists()) return;
            java.io.File tmp = new java.io.File(manifest.getAbsolutePath() + ".tmp");
            try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(manifest)); java.io.FileWriter fw = new java.io.FileWriter(tmp)) {
                String l;
                while ((l = r.readLine()) != null) {
                    if (l.trim().isEmpty()) continue;
                    if (l.startsWith(id + "|")) continue;
                    fw.write(l + "\n");
                }
            }
            if (!manifest.delete()) return;
            tmp.renameTo(manifest);
        } catch (Exception ignored) {}
    }

    private String checksum(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return Long.toHexString(crc.getValue());
    }

    private String checksumFromBytes(byte[] data) {
        return checksum(data);
    }

    /**
     * Read applied migrations from a provider-backed migrations table.
     */
    private java.util.LinkedHashMap<String, String> readAppliedFromDb(com.skyblockexp.ezframework.storage.sql.JdbcStorage jdbc, String providerName) {
        java.util.LinkedHashMap<String, String> map = new java.util.LinkedHashMap<>();
        try {
            // ensure table exists
            jdbc.executeUpdate("CREATE TABLE IF NOT EXISTS ezframework_migrations (id VARCHAR(100) PRIMARY KEY, provider VARCHAR(100), checksum VARCHAR(64), applied_at TIMESTAMP)", null);
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(providerName);
            java.util.List<java.util.Map<String, Object>> rows = jdbc.query("SELECT id, checksum FROM ezframework_migrations WHERE provider = ? ORDER BY applied_at", params);
            for (java.util.Map<String, Object> r : rows) {
                Object id = r.get("id");
                Object cs = r.get("checksum");
                if (id != null) map.put(id.toString(), cs == null ? "" : cs.toString());
            }
        } catch (Exception ignored) {}
        return map;
    }

    private void writeAppliedToDb(com.skyblockexp.ezframework.storage.sql.JdbcStorage jdbc, String id, String provider, String checksum) {
        try {
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(id); params.add(provider); params.add(checksum);
            jdbc.executeUpdate("INSERT INTO ezframework_migrations (id, provider, checksum, applied_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)", params);
        } catch (Exception ignored) {}
    }

    private void removeAppliedFromDb(com.skyblockexp.ezframework.storage.sql.JdbcStorage jdbc, String id) {
        try {
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(id);
            jdbc.executeUpdate("DELETE FROM ezframework_migrations WHERE id = ?", params);
        } catch (Exception ignored) {}
    }

    /**
     * Rollback last applied migration for the given provider. Returns true if a rollback was applied.
     */
    /**
     * Rollback last applied migration for the given provider.
     * @param plugin host plugin used to locate resources
     * @param providerName provider identifier
     * @return true if a rollback was applied, false otherwise
     */
    public boolean rollbackLast(JavaPlugin plugin, String providerName) {
        // prefer DB-backed manifest if provider supports it
        StorageProvider prov = StorageRegistry.get(providerName);
        if (prov instanceof com.skyblockexp.ezframework.storage.sql.JdbcStorage) {
            com.skyblockexp.ezframework.storage.sql.JdbcStorage jdbc = (com.skyblockexp.ezframework.storage.sql.JdbcStorage) prov;
            try {
                // ensure table exists
                jdbc.executeUpdate("CREATE TABLE IF NOT EXISTS ezframework_migrations (id VARCHAR(100) PRIMARY KEY, provider VARCHAR(100), checksum VARCHAR(64), applied_at TIMESTAMP)", null);
                java.util.List<Object> params = new java.util.ArrayList<>();
                params.add(providerName);
                java.util.List<java.util.Map<String, Object>> rows = jdbc.query("SELECT id FROM ezframework_migrations WHERE provider = ? ORDER BY applied_at DESC LIMIT 1", params);
                if (rows.isEmpty()) return false;
                Object idObj = rows.get(0).get("id");
                if (idObj == null) return false;
                String id = idObj.toString();

                // attempt rollback: find resource SQL down file or Java migration
                String upResource = "migrations/" + id + ".sql";
                String downResource = "migrations/" + id + ".down.sql";
                if (pluginResource(plugin, downResource) != null) {
                    try (InputStream in = pluginResource(plugin, downResource)) {
                        String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        if (prov instanceof MigrationCapable) {
                            ((MigrationCapable) prov).executeSqlStatements(SqlSplitter.split(sql));
                            removeAppliedFromDb(jdbc, id);
                            plugin.getLogger().info("Rolled back migration " + id + " for provider " + providerName);
                            return true;
                        }
                    }
                }

                for (Migration m : discoverServiceMigrations(plugin)) {
                    if (!m.id().equals(id)) continue;
                    try {
                        m.rollback(new MigrationContext(plugin, prov));
                        removeAppliedFromDb(jdbc, id);
                        plugin.getLogger().info("Rolled back Java migration " + id + " for provider " + providerName);
                        return true;
                    } catch (UnsupportedOperationException u) {
                        plugin.getLogger().severe("Rollback not supported for migration " + id);
                        return false;
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().severe("Rollback failed: " + ex.getMessage());
                return false;
            }
        }

        // fallback to file-based manifest
        File manifest = new File(plugin.getDataFolder(), "applied_migrations.txt");
        try {
            java.util.List<String> lines = new java.util.ArrayList<>();
            if (!manifest.exists()) return false;
            try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(manifest))) {
                String l;
                while ((l = r.readLine()) != null) {
                    if (!l.trim().isEmpty()) lines.add(l.trim());
                }
            }
            for (int i = lines.size() - 1; i >= 0; i--) {
                String entry = lines.get(i);
                String[] parts = entry.split("\\|", 4);
                if (parts.length < 4) continue;
                String id = parts[0];
                String provider = parts[2];
                if (!providerName.equals(provider)) continue;
                // attempt rollback: find resource SQL down file or Java migration
                // check resources first
                String upResource = "migrations/" + id + ".sql";
                String downResource = "migrations/" + id + ".down.sql";
                if (pluginResource(plugin, downResource) != null) {
                    try (InputStream in = pluginResource(plugin, downResource)) {
                        String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        StorageProvider sp = StorageRegistry.get(providerName);
                        if (sp instanceof MigrationCapable) {
                            ((MigrationCapable) sp).executeSqlStatements(SqlSplitter.split(sql));
                            removeApplied(manifest, id);
                            plugin.getLogger().info("Rolled back migration " + id + " for provider " + providerName);
                            return true;
                        }
                    }
                }

                // try Java migrations via service discovery
                for (Migration m : discoverServiceMigrations(plugin)) {
                    if (!m.id().equals(id)) continue;
                    try {
                        StorageProvider sp = StorageRegistry.get(providerName);
                        m.rollback(new MigrationContext(plugin, sp));
                        removeApplied(manifest, id);
                        plugin.getLogger().info("Rolled back Java migration " + id + " for provider " + providerName);
                        return true;
                    } catch (UnsupportedOperationException u) {
                        plugin.getLogger().severe("Rollback not supported for migration " + id);
                        return false;
                    }
                }
            }
        } catch (Exception ex) {
            plugin.getLogger().severe("Rollback failed: " + ex.getMessage());
        }
        return false;
    }
}

