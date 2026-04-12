package com.skyblockexp.ezframework.config.impl;

import com.skyblockexp.ezframework.EzPlugin;
import com.skyblockexp.ezframework.config.ConfigRegistry;
import com.skyblockexp.ezframework.config.PlatformConfigProvider;

/**
 * Platform provider that registers a default YamlEzConfig under "config.yml".
 */
public class BukkitPlatformConfigProvider implements PlatformConfigProvider {
    @Override
    public void provide(Object plugin, ConfigRegistry registry) throws Exception {
        EzPlugin ezPlugin = (EzPlugin) plugin;
        YamlEzConfig cfg = new YamlEzConfig(ezPlugin, "config.yml");
        cfg.saveDefault();
        cfg.load();
        registry.register(cfg.getFileName(), cfg);
        ezPlugin.getLogger().fine("Registered YamlEzConfig: " + cfg.getFileName());
    }
}
