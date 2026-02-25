package com.skyblockexp.ezframework.config;

import com.skyblockexp.ezframework.bootstrap.Component;
import com.skyblockexp.ezframework.EzPlugin;

import java.util.ServiceLoader;
import java.util.logging.Level;

/**
 * Component responsible for initializing the ConfigRegistry and invoking
 * platform config providers (via ServiceLoader). Placed in core so the API
 * and lifecycle are owned by the framework.
 */
public class ConfigBootstrapComponent implements Component {
    private final EzPlugin plugin;
    private final ConfigRegistry registry;
    private final Iterable<PlatformConfigProvider> injectedProviders;

    public ConfigBootstrapComponent(EzPlugin plugin) {
        this(plugin, null);
    }

    /**
     * Test-friendly constructor allowing direct injection of providers.
     */
    public ConfigBootstrapComponent(EzPlugin plugin, Iterable<PlatformConfigProvider> providers) {
        this.plugin = plugin;
        this.registry = new ConfigRegistry();
        this.injectedProviders = providers;
    }

    @Override
    public void start() throws Exception {
        ConfigRegistry.setDefault(registry);
        plugin.getLogger().fine("ConfigRegistry initialized");

        boolean found = false;
        if (injectedProviders != null) {
            for (PlatformConfigProvider p : injectedProviders) {
                try {
                    p.provide(plugin, registry);
                    found = true;
                    plugin.getLogger().fine("Injected PlatformConfigProvider executed: " + p.getClass().getName());
                } catch (Throwable t) {
                    plugin.getLogger().log(Level.SEVERE, "Injected PlatformConfigProvider failed: " + p.getClass().getName(), t);
                }
            }
        } else {
            ServiceLoader<PlatformConfigProvider> loader = ServiceLoader.load(PlatformConfigProvider.class, getClass().getClassLoader());
            for (PlatformConfigProvider p : loader) {
                try {
                    p.provide(plugin, registry);
                    found = true;
                    plugin.getLogger().fine("PlatformConfigProvider executed: " + p.getClass().getName());
                } catch (Throwable t) {
                    plugin.getLogger().log(Level.SEVERE, "PlatformConfigProvider failed: " + p.getClass().getName(), t);
                }
            }
        }

        if (!found) {
            plugin.getLogger().fine("No PlatformConfigProvider found; no configs registered");
        }
    }

    @Override
    public void stop() throws Exception {
        for (EzConfig cfg : registry.list()) {
            try {
                cfg.save();
            } catch (Throwable t) {
                plugin.getLogger().warning("Failed to stop config: " + cfg.getFileName() + " - " + t.getMessage());
            }
        }
        registry.clear();
        ConfigRegistry.setDefault(null);
    }
}
