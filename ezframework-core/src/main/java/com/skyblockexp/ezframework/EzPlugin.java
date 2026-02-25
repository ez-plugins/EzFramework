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

    /**
     * Default constructor.
     */
    protected EzPlugin() {
        super();
    }

    @Override
    public final void onEnable() {
        // Attempt to auto-register a ConfigBootstrap if the ezframework-config module
        // is present on the classpath. This keeps config loading early and optional.
        try {
            Class<?> cbCls = Class.forName("com.skyblockexp.ezframework.config.ConfigBootstrap", true, getClass().getClassLoader());
            try {
                java.lang.reflect.Constructor<?> ctor = cbCls.getConstructor(org.bukkit.plugin.java.JavaPlugin.class, String.class);
                Object inst = ctor.newInstance(this, "config.yml");
                if (inst instanceof com.skyblockexp.ezframework.bootstrap.Component) {
                    bootstrap.register((com.skyblockexp.ezframework.bootstrap.Component) inst);
                    getLogger().fine("Registered ConfigBootstrap for config.yml");
                }
            } catch (NoSuchMethodException ns) {
                getLogger().fine("ConfigBootstrap found but no suitable constructor");
            }
        } catch (ClassNotFoundException ignored) {
            // ezframework-config not present; skip auto-registration
        } catch (Throwable t) {
            getLogger().severe("Failed to auto-register ConfigBootstrap: " + t.getMessage());
        }
        // Also attempt to auto-register a ConfigManager (new SPI-based multi-config support)
        try {
            Class<?> cmCls = Class.forName("com.skyblockexp.ezframework.config.ConfigManager", true, getClass().getClassLoader());
            try {
                java.lang.reflect.Constructor<?> ctor = cmCls.getConstructor();
                Object inst = ctor.newInstance();
                if (inst instanceof com.skyblockexp.ezframework.bootstrap.Component) {
                    bootstrap.register((com.skyblockexp.ezframework.bootstrap.Component) inst);
                    getLogger().fine("Registered ConfigManager");
                }
            } catch (NoSuchMethodException ns) {
                getLogger().fine("ConfigManager found but no suitable constructor");
            }
        } catch (ClassNotFoundException ignored) {
            // ezframework-config not present; skip
        } catch (Throwable t) {
            getLogger().severe("Failed to auto-register ConfigManager: " + t.getMessage());
        }
        // Register core ConfigBootstrapComponent only if a PlatformConfigProvider exists
        try {
            java.util.ServiceLoader<com.skyblockexp.ezframework.config.PlatformConfigProvider> loader =
                    java.util.ServiceLoader.load(com.skyblockexp.ezframework.config.PlatformConfigProvider.class, getClass().getClassLoader());
            if (loader.iterator().hasNext()) {
                com.skyblockexp.ezframework.config.ConfigBootstrapComponent cbc = new com.skyblockexp.ezframework.config.ConfigBootstrapComponent(this);
                bootstrap.register(cbc);
                getLogger().fine("Registered ConfigBootstrapComponent");
            } else {
                getLogger().fine("No PlatformConfigProvider found; skipping ConfigBootstrapComponent");
            }
        } catch (NoClassDefFoundError | Exception e) {
            // config module not present or failed to construct; skip
            getLogger().fine("ConfigBootstrapComponent not available: " + e.getMessage());
        }
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
    /**
     * Access the underlying {@link Bootstrap} instance.
     * @return bootstrap instance used to manage components
     */
    protected final Bootstrap getBootstrap() {
        return bootstrap;
    }
}
