package com.skyblockexp.ezframework.proxy.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.util.Objects;

/**
 * Velocity event listener that bridges inbound plugin messages to
 * {@link VelocityEzMessenger#dispatch(byte[], String, String)}.
 *
 * <p>Only messages arriving on the EzFramework channel and originating from a
 * backend {@link ServerConnection} (not a client) are forwarded. The event is
 * marked {@link PluginMessageEvent#setResult(PluginMessageEvent.ForwardResult) handled}
 * so Velocity does not forward the raw bytes to other servers.
 */
public final class VelocityPluginMessageListener {

    private final VelocityEzMessenger messenger;
    private final MinecraftChannelIdentifier channelId;

    /**
     * Construct the listener. Called by {@link VelocityBootstrap}.
     *
     * @param messenger the messenger to dispatch received packets to
     */
    VelocityPluginMessageListener(VelocityEzMessenger messenger) {
        this.messenger = Objects.requireNonNull(messenger, "messenger");
        this.channelId = messenger.getDefaultChannelId();
    }

    /**
     * Handle an incoming plugin message event. Ignores messages on other
     * channels or messages sent by clients (players) rather than servers.
     *
     * @param event the plugin message event from Velocity
     */
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(channelId)) {
            return;
        }
        if (!(event.getSource() instanceof ServerConnection source)) {
            return;
        }

        // Mark as handled so Velocity does not re-forward it
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        String serverName = source.getServerInfo().getName();
        Player player = source.getPlayer();
        String playerName = player != null ? player.getUsername() : null;

        messenger.dispatch(event.getData(), serverName, playerName);
    }
}
