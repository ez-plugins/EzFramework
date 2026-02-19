package com.skyblockexp.ezframework.bootstrap.component;

import com.skyblockexp.ezframework.Registry;
import com.skyblockexp.ezframework.bootstrap.Component;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Component that triggers `Registry.initAll()` on start and `Registry.shutdownAll()` on stop
 * for a specific plugin instance.
 */
public class ManagerInitComponent implements Component {
    private final JavaPlugin plugin;

    public ManagerInitComponent(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() throws Exception {
        Registry.forPlugin(plugin).initAll();
    }

    @Override
    public void stop() throws Exception {
        Registry.forPlugin(plugin).shutdownAll();
    }

    @Override
    public void reload() throws Exception {
        Registry.forPlugin(plugin).initAll();
    }
}
