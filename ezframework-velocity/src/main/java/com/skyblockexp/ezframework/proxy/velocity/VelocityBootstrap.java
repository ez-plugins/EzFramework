package com.skyblockexp.ezframework.proxy.velocity;

import com.google.inject.Inject;
import com.skyblockexp.ezframework.proxy.EzChannel;
import com.skyblockexp.ezframework.proxy.EzPacketRegistry;
import com.skyblockexp.ezframework.proxy.EzSerializer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

/**
 * Velocity entry point for the EzFramework messaging layer.
 *
 * <p>This plugin initialises the shared {@link VelocityEzMessenger} and registers
 * the plugin messaging channel with Velocity. Other plugins that want to send or
 * receive {@link com.skyblockexp.ezframework.proxy.EzPacket}s should depend on
 * this plugin and obtain the messenger via {@link #getMessenger()}.
 *
 * <p>If you are building a standalone Velocity plugin that uses EzFramework
 * messaging, you can extend this class or compose it and call
 * {@link #initMessenger(EzPacketRegistry)} yourself instead of relying on the
 * auto-initialised instance.
 */
@Plugin(
        id = "ezframework",
        name = "EzFramework",
        version = "0.2.2",
        description = "Cross-server messaging layer for EzFramework plugins",
        authors = {"ez-plugins"}
)
public class VelocityBootstrap {

    private final ProxyServer proxy;
    private final Logger logger;

    private VelocityEzMessenger messenger;
    private VelocityPluginMessageListener listener;

    /**
     * Constructor called by Velocity's Guice injector.
     *
     * @param proxy  the Velocity proxy server
     * @param logger the plugin logger supplied by Velocity
     */
    @Inject
    public VelocityBootstrap(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    /**
     * Called by Velocity when the proxy starts up. Initialises the messenger
     * with an empty packet registry. Subclasses or companion plugins should
     * call {@link VelocityEzMessenger#registerHandler} and
     * {@link EzPacketRegistry#register} after obtaining the messenger.
     *
     * @param event Velocity proxy initialise event
     */
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        initMessenger(new EzPacketRegistry());
        logger.info("[EzFramework] Velocity messaging layer started on channel '{}'",
                EzChannel.DEFAULT.getName());
    }

    /**
     * Called by Velocity when the proxy shuts down.
     *
     * @param event Velocity proxy shutdown event
     */
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("[EzFramework] Velocity messaging layer stopped.");
    }

    /**
     * Initialise (or re-initialise) the messenger with the given packet registry.
     * This method registers the EzFramework channel with Velocity and installs the
     * plugin message listener. May be called before {@link ProxyInitializeEvent} if
     * needed during testing.
     *
     * @param registry a pre-populated or empty packet registry
     */
    public void initMessenger(EzPacketRegistry registry) {
        EzSerializer serializer = new EzSerializer();

        messenger = new VelocityEzMessenger(proxy, logger, registry, serializer);
        listener = new VelocityPluginMessageListener(messenger);

        proxy.getChannelRegistrar().register(messenger.getDefaultChannelId());
        proxy.getEventManager().register(this, listener);
    }

    /**
     * Return the active messenger. {@code null} before {@link #initMessenger} has been called.
     *
     * @return the messenger, or {@code null} if not yet initialised
     */
    public VelocityEzMessenger getMessenger() {
        return messenger;
    }

    /**
     * Return the Velocity proxy server.
     *
     * @return proxy server
     */
    public ProxyServer getProxy() {
        return proxy;
    }

    /**
     * Return the plugin logger.
     *
     * @return logger
     */
    public Logger getLogger() {
        return logger;
    }
}
