package com.skyblockexp.ezframework.proxy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServerMessageFeatureTest {

    // -------------------------------------------------------------------------
    // ServerMessage.of(EzPacket) — uses DEFAULT channel
    // -------------------------------------------------------------------------

    @Test
    void ofWithPacketUsesDefaultChannel() {
        TestPacket p = new TestPacket();
        ServerMessage msg = ServerMessage.of(p);
        assertSame(p, msg.getPacket());
        assertEquals(EzChannel.DEFAULT, msg.getChannel());
    }

    @Test
    void ofWithPacketRejectsNullPacket() {
        assertThrows(NullPointerException.class, () -> ServerMessage.of(null));
    }

    // -------------------------------------------------------------------------
    // ServerMessage.of(EzPacket, EzChannel) — explicit channel
    // -------------------------------------------------------------------------

    @Test
    void ofWithPacketAndChannelStoresBoth() {
        TestPacket p = new TestPacket();
        EzChannel ch = new EzChannel("custom:channel");
        ServerMessage msg = ServerMessage.of(p, ch);
        assertSame(p, msg.getPacket());
        assertSame(ch, msg.getChannel());
    }

    @Test
    void ofWithPacketAndChannelRejectsNullPacket() {
        assertThrows(NullPointerException.class,
                () -> ServerMessage.of(null, new EzChannel("a:b")));
    }

    @Test
    void ofWithPacketAndChannelRejectsNullChannel() {
        assertThrows(NullPointerException.class,
                () -> ServerMessage.of(new TestPacket(), null));
    }

    // -------------------------------------------------------------------------
    // getPacket() / getChannel()
    // -------------------------------------------------------------------------

    @Test
    void getPacketReturnsSamePacketInstance() {
        TestPacket p = new TestPacket();
        assertSame(p, ServerMessage.of(p).getPacket());
    }

    @Test
    void getChannelReturnsDefaultChannelForOnArgFactory() {
        ServerMessage msg = ServerMessage.of(new TestPacket());
        assertEquals("ezframework:channel", msg.getChannel().getName());
    }

    @Test
    void getChannelReturnsCustomChannel() {
        EzChannel custom = new EzChannel("myplugin:events");
        ServerMessage msg = ServerMessage.of(new TestPacket(), custom);
        assertEquals("myplugin:events", msg.getChannel().getName());
    }

    // -------------------------------------------------------------------------
    // toString()
    // -------------------------------------------------------------------------

    @Test
    void toStringContainsPacketIdAndChannelName() {
        TestPacket p = new TestPacket();
        ServerMessage msg = ServerMessage.of(p);
        String s = msg.toString();
        assertTrue(s.contains("test:servermessage"));
        assertTrue(s.contains("ezframework:channel"));
    }

    // -------------------------------------------------------------------------
    // Test packet
    // -------------------------------------------------------------------------

    static class TestPacket implements EzPacket {
        @Override public String packetId() { return "test:servermessage"; }
    }
}
