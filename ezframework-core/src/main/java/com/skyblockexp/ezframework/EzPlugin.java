package com.skyblockexp.ezframework;

import com.skyblockexp.ezframework.bootstrap.Bootstrap;
import com.skyblockexp.ezframework.bootstrap.Component;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

/**
 * Abstract base plugin that uses a list of {@link Component}s to drive
 * startup and shutdown. Implementations only need to provide their
 * component list by implementing {@link #components()}.
 */
public abstract class EzPlugin extends JavaPlugin {
    private final Bootstrap bootstrap = new Bootstrap(this);

    @Override
    public final void onEnable() {
        List<Component> comps = components();
        if (comps != null) {
            for (Component c : comps) {
                bootstrap.register(Objects.requireNonNull(c, "component"));
            }
        }
        bootstrap.startAll();
    }

    @Override
    public final void onDisable() {
        bootstrap.stopAll();
    }

    /**
     * Provide the list of bootstrap components. This method must return the
     * components that will be registered and started by the framework.
     *
     * @return list of components to register (order matters)
     */
    protected abstract List<Component> components();

    /** Access the underlying {@link Bootstrap} instance. */
    protected final Bootstrap getBootstrap() {
        return bootstrap;
    }
}
