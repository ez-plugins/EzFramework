package com.skyblockexp.ezframework.storage.provider;

import com.skyblockexp.ezframework.query.Condition;
import com.skyblockexp.ezframework.query.Query;
import com.skyblockexp.ezframework.query.QueryableStorage;
import com.skyblockexp.ezframework.storage.StorageProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Simple file-based YAML storage provider. Stores each path as a separate
 * `.yml` file under the plugin data folder `storage/`.
 */
public class YamlStorageProvider implements StorageProvider, QueryableStorage {
    private JavaPlugin plugin;
    private File baseDir;

    /**
     * Public no-arg constructor for provider instantiation.
     */
    public YamlStorageProvider() {}

    @Override
    public String name() {
        return "yaml";
    }

    @Override
    public void init(JavaPlugin plugin) throws Exception {
        this.plugin = plugin;
        this.baseDir = new File(plugin.getDataFolder(), "storage");
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            plugin.getLogger().log(Level.WARNING, "Could not create storage directory: " + baseDir.getAbsolutePath());
        }
    }

    private File fileFor(String path) {
        // sanitize path -> file name
        String safe = path.replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File(baseDir, safe + ".yml");
    }

    @Override
    public void close() throws Exception {
        // nothing to do
    }

    @Override
    public void save(String path, Map<String, Object> data) throws Exception {

            /**
             * Initialize the provider with the plugin instance and create the base
             * storage directory if necessary.
             *
             * @param plugin plugin instance used to determine data folder
             * @throws Exception on initialization failure
             */
        File file = fileFor(path);
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            cfg.set(e.getKey(), e.getValue());
        }
        cfg.save(file);
    }

    @Override
    public Optional<Map<String, Object>> load(String path) throws Exception {
        File file = fileFor(path);
        if (!file.exists()) return Optional.empty();
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> values = new HashMap<>(cfg.getValues(false));
        return Optional.of(values);
    }

    @Override
    public void delete(String path) throws Exception {
        File file = fileFor(path);
        if (file.exists()) file.delete();
    }

    @Override
    public boolean exists(String path) throws Exception {
        return fileFor(path).exists();
    }

    @Override
    public List<String> query(Query q) throws Exception {
        List<String> out = new ArrayList<>();
        if (baseDir == null || !baseDir.exists()) return out;
        File[] files = baseDir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return out;
        for (File f : files) {
            String name = f.getName();
            String id = name.substring(0, name.length() - 4); // strip .yml
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            Map<String, Object> values = new HashMap<>(cfg.getValues(false));
            boolean ok = true;
            for (Map.Entry<String, com.skyblockexp.ezframework.query.Condition> e : q.getConditions().entrySet()) {
                String key = e.getKey();
                Condition c = e.getValue();
                if (!c.matches(values, key)) { ok = false; break; }
            }
            if (ok) out.add(id);
            if (q.getLimit() != null && out.size() >= q.getLimit()) break;
        }
        return out;

            /**
             * Query IDs by {@link Query} conditions by scanning YAML files.
             *
             * @param q query object with conditions/limits
             * @return list of matching IDs
             * @throws Exception on IO errors
             */
    }
}
