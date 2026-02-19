package com.skyblockexp.ezframework.bootstrap;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bootstrap manager that holds multiple {@link Component}s and controls
 * their lifecycle (start/stop/reload). Components are stopped in reverse
 * registration order to help manage dependencies.
 */
public final class Bootstrap {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final List<Component> components = new ArrayList<>();

    public Bootstrap(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = plugin.getLogger();
    }

    public Bootstrap register(Component component) {
        Objects.requireNonNull(component, "component");
        components.add(component);
        return this;
    }

    public List<Component> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public void startAll() {
        for (Component c : components) {
            try {
                logger.info("Starting component: " + c.getClass().getName());
                c.start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error starting component: " + c.getClass().getName(), e);
            }
        }
    }

    public void stopAll() {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            try {
                logger.info("Stopping component: " + c.getClass().getName());
                c.stop();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error stopping component: " + c.getClass().getName(), e);
            }
        }
    }

    public void reloadAll() {
        for (Component c : components) {
            try {
                logger.info("Reloading component: " + c.getClass().getName());
                c.reload();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error reloading component: " + c.getClass().getName(), e);
            }
        }
    }
}
