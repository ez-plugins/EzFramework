package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GuiClickContextFeatureTest {

    // -------------------------------------------------------------------------
    // Constructor & accessors
    // -------------------------------------------------------------------------

    @Test
    void constructorStoresAllFields() {
        GuiPlayer player = stubPlayer();
        GuiItem item = new GuiItem("STONE", 1, "Rock", null);
        GuiClickContext ctx = new GuiClickContext(player, 5, item);

        assertSame(player, ctx.getPlayer());
        assertEquals(5, ctx.getSlot());
        assertSame(item, ctx.getItem());
    }

    @Test
    void itemCanBeNull() {
        GuiClickContext ctx = new GuiClickContext(stubPlayer(), 0, null);
        assertNull(ctx.getItem());
    }

    @Test
    void playerCanBeNull() {
        GuiClickContext ctx = new GuiClickContext(null, 3, null);
        assertNull(ctx.getPlayer());
    }

    @Test
    void slotZeroIsValid() {
        GuiClickContext ctx = new GuiClickContext(stubPlayer(), 0, null);
        assertEquals(0, ctx.getSlot());
    }

    @Test
    void slotHighValueIsStored() {
        GuiClickContext ctx = new GuiClickContext(stubPlayer(), 53, null);
        assertEquals(53, ctx.getSlot());
    }

    @Test
    void getSlotReturnsEveryTestSlot() {
        for (int slot = 0; slot <= 53; slot++) {
            GuiClickContext ctx = new GuiClickContext(stubPlayer(), slot, null);
            assertEquals(slot, ctx.getSlot());
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static GuiPlayer stubPlayer() {
        return new GuiPlayer() {
            @Override public java.util.UUID getUniqueId() { return java.util.UUID.randomUUID(); }
            @Override public String getName() { return "TestPlayer"; }
            @Override public void sendMessage(String message) {}
        };
    }
}
