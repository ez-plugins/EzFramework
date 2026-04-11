package com.skyblockexp.ezframework.proxy.bungee;

import com.skyblockexp.ezframework.proxy.EzChannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Objects;

/**
 * BungeeCord event listener that bridges inbound plugin messages to
 * {@link BungeeEzMessenger#dispatch(byte[], String, String)}.
 *
 * <p>Only messages arriving on the EzFramework channel are forwarded. The event
 * is cancelled so BungeeCord does not re-forward the raw bytes onwards.
 *
 * <p>In BungeeCord, plugin messages always arrive through a player's connection.
 * The player is recorded in {@link EzContext} so handlers can use it if needed,
 * but server-to-server logic should treat messages as coming from the backend
 * server rather than the individual player.
 */
public final class BungeePluginMessageListener implements Listener {

    private final BungeeEzMessenger messenger;
    private final String channelName;

    /**
     * Construct the listener. Called by {@link BungeeBootstrap}.
     *
     * @param messenger the messenger to dispatch received packets to
     */
    BungeePluginMessageListener(BungeeEzMessenger messenger) {
        this.messenger = Objects.requireNonNull(messenger, "messenger");
        this.channelName = EzChannel.DEFAULT.getName();
    }

    /**
     * Handle an incoming plugin message event. Ignores messages on other channels.
     *
     * @param event the plugin message event from BungeeCord
     */
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals(channelName)) {
            return;
        }

        // Cancel so BungeeCord does not re-forward the raw bytes to other servers
        event.setCancelled(true);

        String serverName = null;
        String playerName = null;

        if (event.getSender() instanceof ProxiedPlayer player) {
            playerName = player.getName();
            if (player.getServer() != null) {
                serverName = player.getServer().getInfo().getName();
            }
        }

        messenger.dispatch(event.getData(), serverName, playerName);
    }
}
