package com.skyblockexp.ezframework.proxy.velocity;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;

import static org.mockito.Mockito.*;

public class VelocityPluginMessageListenerTest {

    @Test
    public void onPluginMessage_forwardsServerMessagesToMessenger() {
        // Setup messenger and listener
        com.velocitypowered.api.proxy.ProxyServer proxy = mock(com.velocitypowered.api.proxy.ProxyServer.class);
        Logger logger = mock(Logger.class);
        com.skyblockexp.ezframework.proxy.EzPacketRegistry registry = new com.skyblockexp.ezframework.proxy.EzPacketRegistry();

        VelocityEzMessenger messenger = new VelocityEzMessenger(proxy, logger, registry, new com.skyblockexp.ezframework.proxy.EzSerializer());
        VelocityPluginMessageListener listener = new VelocityPluginMessageListener(messenger);

        // Mock event and server connection
        PluginMessageEvent event = mock(PluginMessageEvent.class);
        ServerConnection serverConnection = mock(ServerConnection.class);
        RegisteredServer server = mock(RegisteredServer.class);
        com.velocitypowered.api.proxy.server.ServerInfo info = mock(com.velocitypowered.api.proxy.server.ServerInfo.class);
        when(info.getName()).thenReturn("backend-1");
        when(server.getServerInfo()).thenReturn(info);
        when(serverConnection.getServerInfo()).thenReturn(info);

        when(event.getIdentifier()).thenReturn(messenger.getDefaultChannelId());
        when(event.getSource()).thenReturn(serverConnection);
        when(event.getData()).thenReturn(new byte[0]);

        // Player null -> playerName null path
        when(serverConnection.getPlayer()).thenReturn((Player) null);

        // Call listener
        listener.onPluginMessage(event);

        // Verify handled
        verify(event).setResult(PluginMessageEvent.ForwardResult.handled());
    }
}
