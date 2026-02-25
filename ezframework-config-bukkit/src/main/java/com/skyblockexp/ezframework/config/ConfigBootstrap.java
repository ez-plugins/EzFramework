package com.skyblockexp.ezframework.config;

import com.skyblockexp.ezframework.bootstrap.Component;
import com.skyblockexp.ezframework.config.migration.ConfigMigrationManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.skyblockexp.ezframework.config.impl.YamlEzConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap component that ensures a plugin config is loaded at startup and
 * runs any registered config migrations. Migrations are discovered via
 * `config_migrations/index.txt` resource and `META-INF/services` service
 * registration for `ConfigMigration` implementations.
 */
public class ConfigBootstrap implements Component {
    private final JavaPlugin plugin;
    private final String fileName;
    private final ConfigMigrationManager manager = new ConfigMigrationManager();

    public ConfigBootstrap(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    @Override
    public void start() throws Exception {
        plugin.getLogger().info("Running ConfigBootstrap for " + fileName);
        YamlEzConfig cfg = new YamlEzConfig(plugin, fileName);
        cfg.saveDefault();
        cfg.load();

        // discover migrations from index resource and service registrations
        List<ConfigMigration> migrations = new ArrayList<>();
        migrations.addAll(discoverIndexedMigrations());
        migrations.addAll(discoverServiceMigrations());

        manager.applyMigrations(cfg, migrations);
    }

    @Override
    public void stop() throws Exception {
        // no-op
    }

    private List<ConfigMigration> discoverIndexedMigrations() {
        List<ConfigMigration> out = new ArrayList<>();
        try (InputStream is = pluginResource(plugin, "config_migrations/index.txt")) {
            if (is == null) return out;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    // treat each line as a fully-qualified migration class name
                    try {
                        Class<?> cls = Class.forName(line, true, plugin.getClass().getClassLoader());
                        Object inst = cls.getDeclaredConstructor().newInstance();
                        if (inst instanceof ConfigMigration) out.add((ConfigMigration) inst);
                    } catch (Throwable t) {
                        plugin.getLogger().severe("Failed to load config migration class from index: " + line + " -> " + t.getMessage());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private List<ConfigMigration> discoverServiceMigrations() {
        List<ConfigMigration> out = new ArrayList<>();
        String svc = ConfigMigration.class.getName();
        try (InputStream is = pluginResource(plugin, "META-INF/services/" + svc)) {
            if (is == null) return out;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    try {
                        Class<?> cls = Class.forName(line, true, plugin.getClass().getClassLoader());
                        Object inst = cls.getDeclaredConstructor().newInstance();
                        if (inst instanceof ConfigMigration) out.add((ConfigMigration) inst);
                    } catch (Throwable t) {
                        plugin.getLogger().severe("Failed to load service config migration class: " + line + " -> " + t.getMessage());
                    }
                }
            }
        } catch (Exception ignored) {}
        return out;
    }

    /**
     * Robust plugin resource lookup similar to core MigrationManager.
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

        InputStream is = plugin.getClass().getClassLoader().getResourceAsStream(path);
        if (is != null) return is;
        return plugin.getClass().getResourceAsStream('/' + path);
    }
}
