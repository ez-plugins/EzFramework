package com.skyblockexp.ezframework.gui.impl;

import com.skyblockexp.ezframework.Manager;
import com.skyblockexp.ezframework.Registry;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manager that exposes the GUI service via the framework `Registry` and
 * participates in the standard lifecycle.
 */
public class GuiManager extends Manager {
    private final JavaPlugin plugin;
    private final BukkitGuiService service;

    /**
     * Create a new GuiManager for the given plugin.
     *
     * @param plugin host plugin
     */
    public GuiManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.service = new BukkitGuiService();
    }

    @Override
    public void init() throws Exception {
        service.init(plugin);
        Registry.forPlugin(plugin).register(GuiManager.class, this);
    }

    @Override
    public void shutdown() throws Exception {
        // nothing to clean here for now; Bukkit event handlers are unregistered automatically on plugin disable
    }

    /**
     * Get the underlying {@link BukkitGuiService} instance.
     *
     * @return service implementation
     */
    public BukkitGuiService getService() { return service; }
}
