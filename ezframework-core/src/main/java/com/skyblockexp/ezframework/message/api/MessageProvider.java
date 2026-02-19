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
    /** Format a message into a string ready for sending. */
    String format(String message);

    /** Send a formatted message to a CommandSender. */
    void send(CommandSender to, String message);

    /** Broadcast a formatted message to all players via the host plugin. */
    void broadcast(String message);

    /** Get the configured prefix (may be empty). */
    String getPrefix();

    /** Set the configured prefix. */
    void setPrefix(String prefix);

    /**
     * Optional initialization hook called with the host plugin during startup.
     * Default no-op; providers that need plugin access (e.g., Adventure
     * integration) can override this.
     */
    default void init(JavaPlugin plugin) throws Exception {}
}
