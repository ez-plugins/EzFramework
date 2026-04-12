package com.skyblockexp.ezframework.bootstrap;

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
 *
 * <p>Construct with a {@link Logger} so this class has no platform dependency.
 */
public final class Bootstrap {
    private final Logger logger;
    private final List<Component> components = new ArrayList<>();

    /**
     * Create a Bootstrap controller using the given logger.
     *
     * @param logger logger for lifecycle messaging (must not be null)
     */
    public Bootstrap(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    /**
     * Register a component for lifecycle management.
     *
     * @param component the component to register
     * @return this Bootstrap instance for chaining
     */
    public Bootstrap register(Component component) {
        Objects.requireNonNull(component, "component");
        components.add(component);
        return this;
    }

    /**
     * Retrieve the registered components in registration order.
     *
     * @return unmodifiable list of registered components
     */
    public List<Component> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * Start all registered components in registration order.
     */
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

    /**
     * Stop all registered components in reverse registration order.
     */
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

    /**
     * Reload all registered components by calling their {@link Component#reload()} hook.
     */
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
