package com.skyblockexp.ezframework.plugin.platform;

import com.google.inject.Inject;
import com.skyblockexp.ezframework.plugin.config.GlobalConfig;
import com.skyblockexp.ezframework.plugin.module.ModuleOrchestrator;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Velocity entry point for the EzFramework manager plugin.
 *
 * <p>Velocity uses annotation-based plugin metadata; the {@code id} matches
 * the plugin folder name and must be lowercase.
 */
@Plugin(
    id          = "ezframework",
    name        = "EzFramework",
    version     = "0.3.0",
    description = "EzFramework runtime module manager",
    authors     = {"ez-plugins"}
)
public final class VelocityManagerPlugin {

    private final ProxyServer         server;
    private final Logger              logger;
    private final Path                dataDirectory;
    private       ModuleOrchestrator  orchestrator;

    @Inject
    public VelocityManagerPlugin(ProxyServer server,
                                  org.slf4j.Logger slf4jLogger,
                                  @DataDirectory Path dataDirectory) {
        this.server        = server;
        // Wrap SLF4J into JUL so ModuleOrchestrator (which uses jul Logger) can consume it
        this.logger        = java.util.logging.Logger.getLogger("EzFramework");
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        GlobalConfig config;
        try {
            config = GlobalConfig.load(dataDirectory.toFile());
        } catch (Exception ex) {
            logger.severe("Failed to load EzFramework config: " + ex.getMessage());
            return;
        }

        orchestrator = new ModuleOrchestrator(logger, config);

        // On Velocity the plugins dir is one level above the data directory
        File pluginsDir = dataDirectory.getParent().toFile();
        orchestrator.scanDescriptors(pluginsDir);

        try {
            orchestrator.downloadMissing();
        } catch (Exception ex) {
            logger.severe("Module download failed: " + ex.getMessage());
            return;
        }

        try {
            orchestrator.initializeModules(this);
        } catch (Exception ex) {
            logger.severe("Module initialization failed: " + ex.getMessage());
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (orchestrator != null) orchestrator.shutdown();
    }
}
