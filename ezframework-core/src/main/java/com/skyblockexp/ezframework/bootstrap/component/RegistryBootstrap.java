package com.skyblockexp.ezframework.bootstrap.component;

import com.skyblockexp.ezframework.Registry;
import com.skyblockexp.ezframework.bootstrap.Component;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Component responsible for initializing the global {@link Registry} with
 * the host plugin instance.
 */
public class RegistryBootstrap implements Component {
    private final JavaPlugin plugin;

    /**
     * Create a registry bootstrap component bound to the plugin.
     *
     * @param plugin host plugin
     */
    public RegistryBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() throws Exception {
        // ensure a per-plugin Registry instance exists
        com.skyblockexp.ezframework.Registry.forPlugin(plugin);
    }

    @Override
    public void stop() throws Exception {
        // nothing to do for registry during stop by default
    }

    @Override
    public void reload() throws Exception {
        // nothing by default
    }
}
