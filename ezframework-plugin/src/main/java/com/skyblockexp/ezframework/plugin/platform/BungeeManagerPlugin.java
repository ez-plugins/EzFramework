package com.skyblockexp.ezframework.plugin.platform;

import com.skyblockexp.ezframework.plugin.config.GlobalConfig;
import com.skyblockexp.ezframework.plugin.module.ModuleOrchestrator;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

/**
 * BungeeCord entry point for the EzFramework manager plugin.
 *
 * <p>The {@code bungee.yml} resource declares this as the main class.
 */
public final class BungeeManagerPlugin extends Plugin {

    private ModuleOrchestrator orchestrator;

    @Override
    public void onEnable() {
        GlobalConfig config;
        try {
            config = GlobalConfig.load(getDataFolder());
        } catch (Exception ex) {
            getLogger().severe("Failed to load EzFramework config: " + ex.getMessage());
            return;
        }

        orchestrator = new ModuleOrchestrator(getLogger(), config);

        // BungeeCord plugins dir is the parent of the plugin data folder
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
