package com.skyblockexp.ezframework.config.impl;

import com.skyblockexp.ezframework.config.EzConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Bukkit-backed EzConfig implementation using YamlConfiguration.
 */
public class YamlEzConfig implements EzConfig {
    private final JavaPlugin plugin;
    private final String fileName;
    private final File file;
    private FileConfiguration cfg;

    public YamlEzConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.file = new File(plugin.getDataFolder(), fileName);
    }

    @Override
    public void load() throws IOException {
        if (!file.exists()) saveDefault();
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void reload() throws IOException {
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void save() throws IOException {
        if (cfg == null) return;
        cfg.save(file);
    }

    @Override
    public void saveDefault() throws IOException {
        if (file.exists()) return;
        // try plugin resource first
        InputStream res = plugin.getResource(fileName);
        if (res != null) {
            plugin.saveResource(fileName, false);
        } else {
            // ensure folder exists
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            // create empty file
            if (!file.exists()) file.createNewFile();
        }
    }

    @Override
    public Path getDataFolder() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public FileConfiguration getConfig() {
        return cfg;
    }
}
