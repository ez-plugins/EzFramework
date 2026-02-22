package com.skyblockexp.ezframework.gui.impl;

import com.skyblockexp.ezframework.bootstrap.Component;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Small bootstrap component that creates and registers the `GuiManager`.
 * Plugins can return an instance of this component from their
 * `EzPlugin.components()` override to enable the GUI manager automatically.
 */
public final class GuiBootstrap implements Component {
    private final JavaPlugin plugin;
    private GuiManager manager;

    /**
     * Create a GUI bootstrap component bound to the host plugin.
     *
     * @param plugin host plugin
     */
    public GuiBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() throws Exception {
        manager = new GuiManager(plugin);
        manager.init();
    }

    @Override
    public void stop() throws Exception {
        if (manager != null) manager.shutdown();
    }
}
