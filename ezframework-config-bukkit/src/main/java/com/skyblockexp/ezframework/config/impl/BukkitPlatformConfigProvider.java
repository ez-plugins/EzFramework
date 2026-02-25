package com.skyblockexp.ezframework.config.impl;

import com.skyblockexp.ezframework.EzPlugin;
import com.skyblockexp.ezframework.config.ConfigRegistry;
import com.skyblockexp.ezframework.config.PlatformConfigProvider;

/**
 * Platform provider that registers a default YamlEzConfig under "config.yml".
 */
public class BukkitPlatformConfigProvider implements PlatformConfigProvider {
    @Override
    public void provide(EzPlugin plugin, ConfigRegistry registry) throws Exception {
        YamlEzConfig cfg = new YamlEzConfig(plugin, "config.yml");
        cfg.saveDefault();
        cfg.load();
        registry.register(cfg.getFileName(), cfg);
        plugin.getLogger().fine("Registered YamlEzConfig: " + cfg.getFileName());
    }
}
