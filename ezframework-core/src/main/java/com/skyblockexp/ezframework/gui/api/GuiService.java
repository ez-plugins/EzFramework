package com.skyblockexp.ezframework.gui.api;

/**
 * Server-agnostic GUI service contract. Implementations should register
 * themselves with {@link com.skyblockexp.ezframework.gui.api.EzGUI#forPlugin(Object)}
 * by calling `registerProvider` during plugin initialization.
 */
public interface GuiService {
    /**
     * Initialize the service with the platform plugin/context. The parameter
     * is intentionally typed as Object to avoid core depending on any
     * server-specific types; implementations should cast as appropriate.
     */
    default void init(Object plugin) {}

    void openMenu(GuiPlayer player, MenuDefinition menu);

    void closeMenu(GuiPlayer player);
}
