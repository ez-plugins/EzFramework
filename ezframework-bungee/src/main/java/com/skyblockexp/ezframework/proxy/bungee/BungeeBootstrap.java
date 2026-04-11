package com.skyblockexp.ezframework.proxy.bungee;

import com.skyblockexp.ezframework.proxy.EzChannel;
import com.skyblockexp.ezframework.proxy.EzPacketRegistry;
import com.skyblockexp.ezframework.proxy.EzSerializer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * BungeeCord entry point for the EzFramework messaging layer.
 *
 * <p>This plugin initialises the shared {@link BungeeEzMessenger} and registers
 * the EzFramework plugin messaging channel with BungeeCord. Other plugins that
 * want to send or receive {@link com.skyblockexp.ezframework.proxy.EzPacket}s
 * should list {@code ezframework} as a dependency in their {@code bungee.yml}
 * and obtain the messenger via {@link #getMessenger()}.
 *
 * <p>If you need to register packet types, do so via
 * {@link BungeeEzMessenger#getRegistry()} after the plugin has enabled:
 * <pre>{@code
 * Plugin ezPlugin = getProxy().getPluginManager().getPlugin("ezframework");
 * BungeeBootstrap bootstrap = (BungeeBootstrap) ezPlugin;
 * bootstrap.getMessenger().getRegistry().register(MyPacket.class);
 * bootstrap.getMessenger().registerHandler(MyPacket.class, (packet, ctx) -> { ... });
 * }</pre>
 */
public class BungeeBootstrap extends Plugin {

    private BungeeEzMessenger messenger;
    private BungeePluginMessageListener listener;

    /**
     * Called by BungeeCord when the plugin enables. Initialises the messenger
     * with an empty packet registry and registers the EzFramework channel.
     */
    @Override
    public void onEnable() {
        initMessenger(new EzPacketRegistry());
        getLogger().info("[EzFramework] BungeeCord messaging layer started on channel '"
                + EzChannel.DEFAULT.getName() + "'");
    }

    /**
     * Called by BungeeCord when the plugin disables.
     */
    @Override
    public void onDisable() {
        getProxy().unregisterChannel(EzChannel.DEFAULT.getName());
        getLogger().info("[EzFramework] BungeeCord messaging layer stopped.");
    }

    /**
     * Initialise (or re-initialise) the messenger with the given packet registry.
     * Registers the EzFramework channel with BungeeCord and installs the plugin
     * message listener. Safe to call before {@link #onEnable()} completes (e.g.
     * during tests).
     *
     * @param registry a pre-populated or empty packet registry
     */
    public void initMessenger(EzPacketRegistry registry) {
        EzSerializer serializer = new EzSerializer();
        messenger = new BungeeEzMessenger(getProxy(), getLogger(), registry, serializer);
        listener = new BungeePluginMessageListener(messenger);

        getProxy().registerChannel(EzChannel.DEFAULT.getName());
        getProxy().getPluginManager().registerListener(this, listener);
    }

    /**
     * Return the active messenger. {@code null} before {@link #onEnable()} has run.
     *
     * @return the messenger, or {@code null} if not yet initialised
     */
    public BungeeEzMessenger getMessenger() {
        return messenger;
    }
}
