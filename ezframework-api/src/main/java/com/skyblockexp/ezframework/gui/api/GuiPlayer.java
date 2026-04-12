package com.skyblockexp.ezframework.gui.api;

import java.util.UUID;

/**
 * Minimal, server-agnostic player handle used in GUI callbacks.
 * Implementations should adapt their native player objects to this
 * interface so core code can remain platform-independent.
 */
public interface GuiPlayer {
    /**
     * Get the player's unique id.
     *
     * @return the player's UUID
     */
    UUID getUniqueId();

    /**
     * Get the player's display name.
     *
     * @return player name
     */
    String getName();

    /**
     * Send a chat/message to the player.
     *
     * @param message the message to send
     */
    void sendMessage(String message);
}
