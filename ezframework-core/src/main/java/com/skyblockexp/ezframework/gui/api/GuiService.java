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
     *
     * @param plugin platform plugin/context instance
     */
    default void init(Object plugin) {}

    /**
     * Open the provided menu for the given player.
     *
     * @param player the target player
     * @param menu the menu definition to open
     */
    void openMenu(GuiPlayer player, MenuDefinition menu);

    /**
     * Close any open menu for the given player.
     *
     * @param player the player whose menu should be closed
     */
    void closeMenu(GuiPlayer player);
}
