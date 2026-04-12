package com.skyblockexp.ezframework.plugin.platform;

import com.skyblockexp.ezframework.plugin.config.GlobalConfig;
import com.skyblockexp.ezframework.plugin.module.ModuleOrchestrator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Bukkit/Paper entry point for the EzFramework manager plugin.
 *
 * <p>The {@code plugin.yml} resource declares this as the main class so that
 * Bukkit/Paper server instances load it automatically.
 */
public final class BukkitManagerPlugin extends JavaPlugin {

    private ModuleOrchestrator orchestrator;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        GlobalConfig config;
        try {
            config = GlobalConfig.load(getDataFolder());
        } catch (Exception ex) {
            getLogger().severe("Failed to load EzFramework config: " + ex.getMessage());
            return;
        }

        orchestrator = new ModuleOrchestrator(getLogger(), config);

        File pluginsDir = getDataFolder().getParentFile();
        orchestrator.scanDescriptors(pluginsDir);

        try {
            orchestrator.downloadMissing();
        } catch (Exception ex) {
            getLogger().severe("Module download failed: " + ex.getMessage());
            return;
        }

        try {
            orchestrator.initializeModules(this);
        } catch (Exception ex) {
            getLogger().severe("Module initialization failed: " + ex.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (orchestrator != null) orchestrator.shutdown();
    }
}
