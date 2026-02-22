package com.skyblockexp.ezframework.gui.impl;

import com.skyblockexp.ezframework.gui.api.GuiPlayer;
import org.bukkit.entity.Player;

/**
 * Small utility adapters for converting Bukkit types to core GUI abstractions.
 */
public final class BukkitGuiAdapters {
    private BukkitGuiAdapters() {}

    /**
     * Wrap a Bukkit {@link Player} as a {@link GuiPlayer} for use by core APIs.
     *
     * @param player the Bukkit player to wrap
     * @return a platform-agnostic {@link GuiPlayer}
     */
    public static GuiPlayer wrap(Player player) {
        return new BukkitWrappedGuiPlayer(player);
    }

    /**
     * {@link GuiPlayer} adapter backed by a Bukkit {@link Player} instance.
     */
    public static class BukkitWrappedGuiPlayer implements GuiPlayer {
        private final Player player;

        /**
         * Create a wrapper for the given Bukkit player.
         *
         * @param player the Bukkit player
         */
        public BukkitWrappedGuiPlayer(Player player) {
            this.player = player;
        }

        /**
         * Access the underlying Bukkit {@link Player}.
         *
         * @return the wrapped Bukkit player
         */
        public Player getPlayer() { return player; }

        @Override
        public java.util.UUID getUniqueId() { return player.getUniqueId(); }

        @Override
        public String getName() { return player.getName(); }

        @Override
        public void sendMessage(String message) { player.sendMessage(message); }
    }
}
