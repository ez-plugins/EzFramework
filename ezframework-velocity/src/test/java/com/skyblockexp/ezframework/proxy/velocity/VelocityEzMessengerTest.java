package com.skyblockexp.ezframework.proxy.velocity;

import com.skyblockexp.ezframework.proxy.EzPacket;
import com.skyblockexp.ezframework.proxy.EzPacketHandler;
import com.skyblockexp.ezframework.proxy.EzPacketRegistry;
import com.skyblockexp.ezframework.proxy.EzSerializer;
import com.skyblockexp.ezframework.proxy.ServerMessage;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VelocityEzMessengerTest {

    public static class TestPacket implements EzPacket {
        public String text;

        public TestPacket() {
        }

        public TestPacket(String text) {
            this.text = text;
        }

        @Override
        public String packetId() {
            return "test:ping";
        }
    }

    @Test
    public void dispatch_deserializesAndInvokesHandler() {
        ProxyServer proxy = mock(ProxyServer.class);
        Logger logger = mock(Logger.class);

        EzPacketRegistry registry = new EzPacketRegistry();
        registry.register(TestPacket.class);

        VelocityEzMessenger messenger = new VelocityEzMessenger(proxy, logger, registry, new EzSerializer());

        @SuppressWarnings("unchecked")
        EzPacketHandler<TestPacket> handler = mock(EzPacketHandler.class);
        messenger.registerHandler(TestPacket.class, handler);

        TestPacket pkt = new TestPacket("hello");
        byte[] data = new EzSerializer().serialize(pkt);

        messenger.dispatch(data, "backend-1", "playerA");

        ArgumentCaptor<TestPacket> captor = ArgumentCaptor.forClass(TestPacket.class);
        verify(handler, times(1)).handle(captor.capture(), any());
        assertEquals("hello", captor.getValue().text);
    }

    @Test
    public void send_resolvesServerAndSendsPluginMessage() {
        ProxyServer proxy = mock(ProxyServer.class);
        Logger logger = mock(Logger.class);

        RegisteredServer server = mock(RegisteredServer.class);
        ServerInfo info = mock(ServerInfo.class);
        when(info.getName()).thenReturn("backend-1");
        when(server.getServerInfo()).thenReturn(info);
        when(server.sendPluginMessage(any(ChannelIdentifier.class), any(byte[].class))).thenReturn(true);

        when(proxy.getServer("backend-1")).thenReturn(Optional.of(server));

        EzPacketRegistry registry = new EzPacketRegistry();
        VelocityEzMessenger messenger = new VelocityEzMessenger(proxy, logger, registry, new EzSerializer());

        TestPacket pkt = new TestPacket("ping");
        ServerMessage msg = ServerMessage.of(pkt);
        messenger.send("backend-1", msg);

        verify(server, times(1)).sendPluginMessage(any(ChannelIdentifier.class), any(byte[].class));
    }

    @Test
    public void broadcast_sendsToAllRegisteredServers() {
        ProxyServer proxy = mock(ProxyServer.class);
        Logger logger = mock(Logger.class);

        RegisteredServer server1 = mock(RegisteredServer.class);
        ServerInfo info1 = mock(ServerInfo.class);
        when(info1.getName()).thenReturn("s1");
        when(server1.getServerInfo()).thenReturn(info1);
        when(server1.sendPluginMessage(any(ChannelIdentifier.class), any(byte[].class))).thenReturn(true);

        RegisteredServer server2 = mock(RegisteredServer.class);
        ServerInfo info2 = mock(ServerInfo.class);
        when(info2.getName()).thenReturn("s2");
        when(server2.getServerInfo()).thenReturn(info2);
        when(server2.sendPluginMessage(any(ChannelIdentifier.class), any(byte[].class))).thenReturn(false);

        when(proxy.getAllServers()).thenReturn((Collection) List.of(server1, server2));

        EzPacketRegistry registry = new EzPacketRegistry();
        VelocityEzMessenger messenger = new VelocityEzMessenger(proxy, logger, registry, new EzSerializer());

        TestPacket pkt = new TestPacket("broadcast");
        messenger.broadcast(ServerMessage.of(pkt));

        verify(server1, times(1)).sendPluginMessage(any(ChannelIdentifier.class), any(byte[].class));
        verify(server2, times(1)).sendPluginMessage(any(ChannelIdentifier.class), any(byte[].class));
    }
}
