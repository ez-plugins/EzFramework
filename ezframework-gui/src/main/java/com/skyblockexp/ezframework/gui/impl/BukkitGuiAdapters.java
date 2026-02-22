package com.skyblockexp.ezframework.gui.impl;

import com.skyblockexp.ezframework.gui.api.GuiPlayer;
import org.bukkit.entity.Player;

/**
 * Small utility adapters for converting Bukkit types to core GUI abstractions.
 */
public final class BukkitGuiAdapters {
    private BukkitGuiAdapters() {}

    public static GuiPlayer wrap(Player player) {
        return new BukkitWrappedGuiPlayer(player);
    }

    public static class BukkitWrappedGuiPlayer implements GuiPlayer {
        private final Player player;

        public BukkitWrappedGuiPlayer(Player player) {
            this.player = player;
        }

        public Player getPlayer() { return player; }

        @Override
        public java.util.UUID getUniqueId() { return player.getUniqueId(); }

        @Override
        public String getName() { return player.getName(); }

        @Override
        public void sendMessage(String message) { player.sendMessage(message); }
    }
}
