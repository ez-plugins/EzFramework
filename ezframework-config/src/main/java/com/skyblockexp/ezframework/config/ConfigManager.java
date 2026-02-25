package com.skyblockexp.ezframework.config;

import com.skyblockexp.ezframework.bootstrap.Component;
import com.skyblockexp.ezframework.config.ConfigMigration;
import com.skyblockexp.ezframework.config.migration.ConfigMigrationManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Component responsible for discovering and loading all `EzConfig` instances
 * provided by the plugin via SPI (`META-INF/services/...EzConfigProvider`).
 */
public class ConfigManager implements Component {
    private final Map<String, EzConfig> configs = new LinkedHashMap<>();
    private final ConfigMigrationManager migrationManager = new ConfigMigrationManager();

    public ConfigManager() {
    }

    @Override
    public void start() throws Exception {
        java.util.logging.Logger.getLogger(ConfigManager.class.getName()).info("Starting ConfigManager discovery");
        // Discover EzConfigProvider implementations using the context classloader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ServiceLoader<EzConfigProvider> loader = ServiceLoader.load(EzConfigProvider.class, cl);
        for (EzConfigProvider p : loader) {
            try {
            EzConfig cfg = p.create();
                if (cfg == null) continue;
                register(cfg);
                cfg.saveDefault();
                cfg.load();

                List<ConfigMigration> migrations = new ArrayList<>();
                migrations.addAll(discoverIndexedMigrations());
                migrations.addAll(discoverServiceMigrations());

                migrationManager.applyMigrations(cfg, migrations);
                java.util.logging.Logger.getLogger(ConfigManager.class.getName()).info("Loaded config: " + cfg.getFileName());
            } catch (Throwable t) {
                java.util.logging.Logger.getLogger(ConfigManager.class.getName()).severe("Failed to load EzConfig from provider: " + t.getMessage());
            }
        }
    }

    @Override
    public void stop() throws Exception {
        // no-op
    }

    public void register(EzConfig cfg) {
        if (cfg == null) return;
        configs.put(cfg.getFileName(), cfg);
    }

    public EzConfig get(String fileName) {
        return configs.get(fileName);
    }

    public List<EzConfig> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(configs.values()));
    }

    private List<ConfigMigration> discoverIndexedMigrations() {
        List<ConfigMigration> out = new ArrayList<>();
        try (InputStream is = pluginResource("config_migrations/index.txt")) {
            if (is == null) return out;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    try {
                        Class<?> cls = Class.forName(line, true, Thread.currentThread().getContextClassLoader());
                        Object inst = cls.getDeclaredConstructor().newInstance();
                        if (inst instanceof ConfigMigration) out.add((ConfigMigration) inst);
                    } catch (Throwable t) {
                        java.util.logging.Logger.getLogger(ConfigManager.class.getName()).severe("Failed to load config migration class from index: " + line + " -> " + t.getMessage());
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
        try (InputStream is = pluginResource("META-INF/services/" + svc)) {
            if (is == null) return out;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    try {
                        Class<?> cls = Class.forName(line, true, Thread.currentThread().getContextClassLoader());
                        Object inst = cls.getDeclaredConstructor().newInstance();
                        if (inst instanceof ConfigMigration) out.add((ConfigMigration) inst);
                    } catch (Throwable t) {
                        java.util.logging.Logger.getLogger(ConfigManager.class.getName()).severe("Failed to load service config migration class: " + line + " -> " + t.getMessage());
                    }
                }
            }
        } catch (Exception ignored) {}
        return out;
    }

    private InputStream pluginResource(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream(path);
        if (is != null) return is;
        return ConfigManager.class.getResourceAsStream('/' + path);
    }
}
