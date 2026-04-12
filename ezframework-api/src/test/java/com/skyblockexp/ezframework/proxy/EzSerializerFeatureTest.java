package com.skyblockexp.ezframework.proxy;

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EzSerializerFeatureTest {

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    @Test
    void defaultConstructorCreatesInstance() {
        assertNotNull(new EzSerializer());
    }

    @Test
    void customGsonConstructorCreatesInstance() {
        assertNotNull(new EzSerializer(new GsonBuilder().create()));
    }

    @Test
    void customGsonConstructorRejectsNull() {
        assertThrows(NullPointerException.class, () -> new EzSerializer(null));
    }

    // -------------------------------------------------------------------------
    // serialize()
    // -------------------------------------------------------------------------

    @Test
    void serializeProducesNonEmptyBytes() {
        EzSerializer ser = new EzSerializer();
        byte[] bytes = ser.serialize(new BalancePacket(42));
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void serializeRejectsNullPacket() {
        assertThrows(NullPointerException.class, () -> new EzSerializer().serialize(null));
    }

    @Test
    void serializedBytesContainPacketId() {
        EzSerializer ser = new EzSerializer();
        byte[] bytes = ser.serialize(new BalancePacket(100));
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(json.contains("test:balance"), "serialized JSON must include the packet ID");
    }

    // -------------------------------------------------------------------------
    // deserialize()
    // -------------------------------------------------------------------------

    @Test
    void deserializeReconstructsPacket() {
        EzSerializer ser = new EzSerializer();
        EzPacketRegistry reg = new EzPacketRegistry();
        reg.register(BalancePacket.class);

        BalancePacket original = new BalancePacket(99);
        byte[] bytes = ser.serialize(original);
        EzPacket result = ser.deserialize(bytes, reg);

        assertInstanceOf(BalancePacket.class, result);
        assertEquals(99, ((BalancePacket) result).amount);
    }

    @Test
    void deserializeRejectsNullData() {
        EzPacketRegistry reg = new EzPacketRegistry();
        assertThrows(NullPointerException.class, () -> new EzSerializer().deserialize(null, reg));
    }

    @Test
    void deserializeRejectsNullRegistry() {
        byte[] bytes = new EzSerializer().serialize(new BalancePacket(0));
        assertThrows(NullPointerException.class, () -> new EzSerializer().deserialize(bytes, null));
    }

    @Test
    void deserializeThrowsForUnknownPacketId() {
        EzSerializer ser = new EzSerializer();
        EzPacketRegistry reg = new EzPacketRegistry(); // empty registry
        byte[] bytes = ser.serialize(new BalancePacket(1));
        assertThrows(EzSerializer.EzSerializerException.class, () -> ser.deserialize(bytes, reg));
    }

    @Test
    void deserializeThrowsForMalformedJson() {
        EzPacketRegistry reg = new EzPacketRegistry();
        reg.register(BalancePacket.class);
        byte[] bad = "not json".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThrows(EzSerializer.EzSerializerException.class, () -> new EzSerializer().deserialize(bad, reg));
    }

    @Test
    void deserializeThrowsForNonNamespacedId() {
        // Manually craft a JSON envelope with a non-namespaced ID
        String json = "{\"id\":\"nonnamespaced\",\"data\":{}}";
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        EzPacketRegistry reg = new EzPacketRegistry();
        assertThrows(EzSerializer.EzSerializerException.class, () -> new EzSerializer().deserialize(bytes, reg));
    }

    // -------------------------------------------------------------------------
    // Round-trip with custom Gson
    // -------------------------------------------------------------------------

    @Test
    void roundTripWithCustomGson() {
        EzSerializer ser = new EzSerializer(new GsonBuilder().create());
        EzPacketRegistry reg = new EzPacketRegistry();
        reg.register(BalancePacket.class);

        BalancePacket original = new BalancePacket(777);
        byte[] bytes = ser.serialize(original);
        BalancePacket result = (BalancePacket) ser.deserialize(bytes, reg);
        assertEquals(777, result.amount);
    }

    // -------------------------------------------------------------------------
    // Test packet
    // -------------------------------------------------------------------------

    public static class BalancePacket implements EzPacket {
        int amount;
        public BalancePacket() {}
        public BalancePacket(int amount) { this.amount = amount; }
        @Override public String packetId() { return "test:balance"; }
    }
}
