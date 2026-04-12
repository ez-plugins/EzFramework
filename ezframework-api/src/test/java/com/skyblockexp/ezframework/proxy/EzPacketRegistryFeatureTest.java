package com.skyblockexp.ezframework.proxy;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class EzPacketRegistryFeatureTest {

    // -------------------------------------------------------------------------
    // register(Class<T>) — auto ID from packetId()
    // -------------------------------------------------------------------------

    @Test
    void registerByClassUsesPacketId() {
        EzPacketRegistry reg = new EzPacketRegistry();
        reg.register(PingPacket.class);
        assertTrue(reg.isRegistered("test:ping"));
    }

    @Test
    void registerByClassReturnsRegistryForChaining() {
        EzPacketRegistry reg = new EzPacketRegistry();
        assertSame(reg, reg.register(PingPacket.class));
    }

    @Test
    void registerByClassRejectsNullClass() {
        EzPacketRegistry reg = new EzPacketRegistry();
        assertThrows(NullPointerException.class, () -> reg.register((Class<EzPacket>) null));
    }

    @Test
    void registerByClassFailsForNonNamespacedPacketId() {
        EzPacketRegistry reg = new EzPacketRegistry();
        assertThrows(IllegalArgumentException.class, () -> reg.register(BadIdPacket.class));
    }

    // -------------------------------------------------------------------------
    // register(String, Class<T>) — explicit ID override
    // -------------------------------------------------------------------------

    @Test
    void registerWithExplicitIdOverridesPacketId() {
        EzPacketRegistry reg = new EzPacketRegistry();
        reg.register("test:custom", PingPacket.class);
        assertTrue(reg.isRegistered("test:custom"));
        assertFalse(reg.isRegistered("test:ping"));
    }

    @Test
    void registerWithExplicitIdRejectsNullId() {
        EzPacketRegistry reg = new EzPacketRegistry();
        assertThrows(NullPointerException.class, () -> reg.register(null, PingPacket.class));
    }

    @Test
    void registerWithExplicitIdRejectsNullClass() {
        EzPacketRegistry reg = new EzPacketRegistry();
        assertThrows(NullPointerException.class, () -> reg.register("test:x", null));
    }

    @Test
    void registerWithExplicitIdRejectsNonNamespacedId() {
        EzPacketRegistry reg = new EzPacketRegistry();
        assertThrows(IllegalArgumentException.class, () -> reg.register("nocolon", PingPacket.class));
    }

    @Test
    void registerWithExplicitIdReturnsRegistryForChaining() {
        EzPacketRegistry reg = new EzPacketRegistry();
        assertSame(reg, reg.register("test:custom", PingPacket.class));
    }

    // -------------------------------------------------------------------------
    // getClass()
    // -------------------------------------------------------------------------

    @Test
    void getClassReturnsRegisteredClass() {
        EzPacketRegistry reg = new EzPacketRegistry();
        reg.register(PingPacket.class);
        Optional<Class<? extends EzPacket>> cls = reg.getClass("test:ping");
        assertTrue(cls.isPresent());
        assertEquals(PingPacket.class, cls.get());
    }

    @Test
    void getClassReturnsEmptyForUnregisteredId() {
        EzPacketRegistry reg = new EzPacketRegistry();
        assertFalse(reg.getClass("test:missing").isPresent());
    }

    // -------------------------------------------------------------------------
    // isRegistered()
    // -------------------------------------------------------------------------

    @Test
    void isRegisteredTrueAfterRegistration() {
        EzPacketRegistry reg = new EzPacketRegistry();
        reg.register(PingPacket.class);
        assertTrue(reg.isRegistered("test:ping"));
    }

    @Test
    void isRegisteredFalseForUnknownId() {
        assertFalse(new EzPacketRegistry().isRegistered("test:unknown"));
    }

    // -------------------------------------------------------------------------
    // size()
    // -------------------------------------------------------------------------

    @Test
    void sizeIsZeroInitially() {
        assertEquals(0, new EzPacketRegistry().size());
    }

    @Test
    void sizeReflectsRegisteredCount() {
        EzPacketRegistry reg = new EzPacketRegistry();
        reg.register(PingPacket.class).register("test:pong", PongPacket.class);
        assertEquals(2, reg.size());
    }

    // -------------------------------------------------------------------------
    // requireNamespaced()
    // -------------------------------------------------------------------------

    @Test
    void requireNamespacedPassesForValidId() {
        assertDoesNotThrow(() -> EzPacketRegistry.requireNamespaced("ns:action", "ctx"));
    }

    @Test
    void requireNamespacedThrowsForNoColon() {
        assertThrows(IllegalArgumentException.class,
                () -> EzPacketRegistry.requireNamespaced("nocoion", "ctx"));
    }

    @Test
    void requireNamespacedThrowsWhenColonAtStart() {
        assertThrows(IllegalArgumentException.class,
                () -> EzPacketRegistry.requireNamespaced(":action", "ctx"));
    }

    @Test
    void requireNamespacedThrowsWhenColonAtEnd() {
        assertThrows(IllegalArgumentException.class,
                () -> EzPacketRegistry.requireNamespaced("ns:", "ctx"));
    }

    @Test
    void requireNamespacedThrowsForMultipleColons() {
        assertThrows(IllegalArgumentException.class,
                () -> EzPacketRegistry.requireNamespaced("ns:action:extra", "ctx"));
    }

    // -------------------------------------------------------------------------
    // Test packet implementations
    // -------------------------------------------------------------------------

    public static class PingPacket implements EzPacket {
        public PingPacket() {}
        @Override public String packetId() { return "test:ping"; }
    }

    public static class PongPacket implements EzPacket {
        public PongPacket() {}
        @Override public String packetId() { return "test:pong"; }
    }

    /** Packet with an invalid (non-namespaced) ID. */
    public static class BadIdPacket implements EzPacket {
        public BadIdPacket() {}
        @Override public String packetId() { return "no-namespace"; }
    }
}
