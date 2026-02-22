package com.skyblockexp.ezframework.gui.api;

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Per-plugin GUI manager accessor. Implementations should register
 * a concrete `GuiService` implementation at runtime (e.g. from
 * `ezframework-gui`) via `registerProvider` or by providing a
 * ServiceLoader entry.
 */
public final class EzGUI {
    private static final Logger LOGGER = Logger.getLogger(EzGUI.class.getName());
    private static final Map<Object, EzGUI> INSTANCES = new ConcurrentHashMap<>();

    private final Object pluginContext;
    private volatile GuiService provider;

    private EzGUI(Object pluginContext) {
        this.pluginContext = Objects.requireNonNull(pluginContext, "pluginContext");

        GuiService p = null;

        try {
            ServiceLoader<GuiService> loader = ServiceLoader.load(GuiService.class);
            for (GuiService s : loader) {
                p = s;
                try {
                    p.init(pluginContext);
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "GuiService provider init failed", t);
                    p = null;
                    continue;
                }
                LOGGER.log(Level.INFO, "Loaded GuiService provider: {0}", p.getClass().getName());
                break;
            }
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "ServiceLoader lookup for GuiService failed", t);
        }

        if (p == null) {
            p = new GuiService() {
                @Override
                public void init(Object plugin) {}

                @Override
                public void openMenu(GuiPlayer player, MenuDefinition menu) {}

                @Override
                public void closeMenu(GuiPlayer player) {}
            };
            LOGGER.log(Level.FINE, "No GuiService provider found; using no-op provider");
        }

        this.provider = p;
    }

    public static EzGUI forPlugin(Object pluginContext) {
        return INSTANCES.computeIfAbsent(pluginContext, EzGUI::new);
    }

    public GuiService getProvider() {
        return provider;
    }

    public void registerProvider(GuiService provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    public void openMenu(GuiPlayer player, MenuDefinition menu) {
        provider.openMenu(player, menu);
    }

    public void closeMenu(GuiPlayer player) {
        provider.closeMenu(player);
    }
}
