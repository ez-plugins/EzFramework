package com.skyblockexp.ezframework.proxy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EzContextFeatureTest {

    // -------------------------------------------------------------------------
    // 3-arg constructor
    // -------------------------------------------------------------------------

    @Test
    void threeArgConstructorStoresAllFields() {
        EzContext ctx = new EzContext("Alice", "lobby", "survival");
        assertEquals("Alice", ctx.getPlayerName());
        assertEquals("lobby", ctx.getSourceServer());
        assertEquals("survival", ctx.getTargetServer());
    }

    @Test
    void threeArgConstructorAllowsNulls() {
        EzContext ctx = new EzContext(null, null, null);
        assertNull(ctx.getPlayerName());
        assertNull(ctx.getSourceServer());
        assertNull(ctx.getTargetServer());
    }

    @Test
    void threeArgConstructorWithNullPlayer() {
        EzContext ctx = new EzContext(null, "hub", "game");
        assertNull(ctx.getPlayerName());
        assertEquals("hub", ctx.getSourceServer());
        assertEquals("game", ctx.getTargetServer());
    }

    // -------------------------------------------------------------------------
    // EzContext.of() factory
    // -------------------------------------------------------------------------

    @Test
    void ofFactorySetsNullPlayerName() {
        EzContext ctx = EzContext.of("hub", "minigame");
        assertNull(ctx.getPlayerName());
        assertEquals("hub", ctx.getSourceServer());
        assertEquals("minigame", ctx.getTargetServer());
    }

    @Test
    void ofFactoryWithNullServersAllowed() {
        EzContext ctx = EzContext.of(null, null);
        assertNull(ctx.getPlayerName());
        assertNull(ctx.getSourceServer());
        assertNull(ctx.getTargetServer());
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    @Test
    void getPlayerNameReturnsValueFromConstructor() {
        assertEquals("Bob", new EzContext("Bob", "a", "b").getPlayerName());
    }

    @Test
    void getSourceServerReturnsValueFromConstructor() {
        assertEquals("hub", new EzContext("p", "hub", "b").getSourceServer());
    }

    @Test
    void getTargetServerReturnsValueFromConstructor() {
        assertEquals("survival", new EzContext("p", "a", "survival").getTargetServer());
    }

    // -------------------------------------------------------------------------
    // toString()
    // -------------------------------------------------------------------------

    @Test
    void toStringContainsAllFields() {
        EzContext ctx = new EzContext("Alice", "lobby", "survival");
        String s = ctx.toString();
        assertTrue(s.contains("Alice"));
        assertTrue(s.contains("lobby"));
        assertTrue(s.contains("survival"));
    }
}
