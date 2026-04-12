package com.skyblockexp.ezframework.proxy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EzChannelFeatureTest {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    @Test
    void constructorRejectsNullName() {
        assertThrows(NullPointerException.class, () -> new EzChannel(null));
    }

    @Test
    void constructorStoresName() {
        EzChannel ch = new EzChannel("myplugin:data");
        assertEquals("myplugin:data", ch.getName());
    }

    // -------------------------------------------------------------------------
    // getName()
    // -------------------------------------------------------------------------

    @Test
    void getNameReturnsSameStringPassedToConstructor() {
        String name = "ezframework:channel";
        assertEquals(name, new EzChannel(name).getName());
    }

    // -------------------------------------------------------------------------
    // parts()
    // -------------------------------------------------------------------------

    @Test
    void partsReturnsTwoPartArrayForNamespaceColonKey() {
        EzChannel ch = new EzChannel("ns:key");
        String[] parts = ch.parts();
        assertEquals(2, parts.length);
        assertEquals("ns", parts[0]);
        assertEquals("key", parts[1]);
    }

    @Test
    void partsReturnsSingleElementForNameWithNoColon() {
        EzChannel ch = new EzChannel("nocoion");
        String[] parts = ch.parts();
        assertEquals(1, parts.length);
        assertEquals("nocoion", parts[0]);
    }

    @Test
    void partsWorksWithDefaultChannel() {
        String[] parts = EzChannel.DEFAULT.parts();
        assertEquals(2, parts.length);
        assertEquals("ezframework", parts[0]);
        assertEquals("channel", parts[1]);
    }

    // -------------------------------------------------------------------------
    // DEFAULT constant
    // -------------------------------------------------------------------------

    @Test
    void defaultChannelHasCorrectName() {
        assertEquals("ezframework:channel", EzChannel.DEFAULT.getName());
    }

    @Test
    void defaultConstantIsNotNull() {
        assertNotNull(EzChannel.DEFAULT);
    }

    // -------------------------------------------------------------------------
    // equals() / hashCode()
    // -------------------------------------------------------------------------

    @Test
    void equalsReturnsTrueForSameName() {
        EzChannel c1 = new EzChannel("a:b");
        EzChannel c2 = new EzChannel("a:b");
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentName() {
        assertNotEquals(new EzChannel("a:b"), new EzChannel("a:c"));
    }

    @Test
    void equalsSelf() {
        EzChannel ch = new EzChannel("a:b");
        assertEquals(ch, ch);
    }

    @Test
    void equalsNullReturnsFalse() {
        assertNotEquals(new EzChannel("a:b"), null);
    }
}
