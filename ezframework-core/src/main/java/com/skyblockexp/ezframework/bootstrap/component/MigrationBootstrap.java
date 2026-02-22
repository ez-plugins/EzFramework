package com.skyblockexp.ezframework.bootstrap.component;

import com.skyblockexp.ezframework.bootstrap.Component;
import com.skyblockexp.ezframework.storage.migration.MigrationManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bootstrap component that applies storage migrations on startup.
 */
public class MigrationBootstrap implements Component {
    private final MigrationManager manager = new MigrationManager();
    private final JavaPlugin plugin;

    /**
     * Create a migration bootstrap component bound to the plugin.
     *
     * @param plugin host plugin
     */
    public MigrationBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() throws Exception {
        plugin.getLogger().info("Running MigrationBootstrap");
        manager.applyMigrations(plugin);
    }

    @Override
    public void stop() throws Exception {
        // no-op
    }

    @Override
    public void reload() throws Exception {
        // no-op
    }
}
