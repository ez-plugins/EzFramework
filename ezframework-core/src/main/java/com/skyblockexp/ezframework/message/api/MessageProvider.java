package com.skyblockexp.ezframework.message.api;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Abstraction for message formatting and delivery. Implementations may
 * support MiniMessage/Adventure or simple legacy color codes.
 *
 * Located in `message.api` to make extraction into a separate library easier
 * in the future. Keep this interface minimal and free of implementation
 * details to simplify reuse.
 */
public interface MessageProvider {
    /**
     * Format a message into a string ready for sending.
     *
     * @param message raw message text
     * @return formatted message
     */
    String format(String message);

    /**
     * Send a formatted message to a CommandSender.
     *
     * @param to recipient
     * @param message message text
     */
    void send(CommandSender to, String message);

    /**
     * Broadcast a formatted message to all players via the host plugin.
     *
     * @param message message text
     */
    void broadcast(String message);

    /**
     * Get the configured prefix (may be empty).
     *
     * @return configured prefix
     */
    String getPrefix();

    /**
     * Set the configured prefix.
     *
     * @param prefix prefix text (may be null)
     */
    void setPrefix(String prefix);

    /**
     * Optional initialization hook called with the host plugin during startup.
     * Default no-op; providers that need plugin access (e.g., Adventure
     * integration) can override this.
     *
     * @param plugin host plugin instance
     * @throws Exception when initialization fails
     */
    default void init(JavaPlugin plugin) throws Exception {}
}
