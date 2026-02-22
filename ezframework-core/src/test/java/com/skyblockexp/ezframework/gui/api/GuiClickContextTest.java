package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

public class GuiClickContextTest {

    @Test
    public void canConstructViaReflectionWithNullPlayer() throws Exception {
        Constructor<GuiClickContext> c = (Constructor<GuiClickContext>) GuiClickContext.class.getDeclaredConstructors()[0];
        c.setAccessible(true);
        GuiItem it = new GuiItem("STONE", 1, "Stone", null);
        GuiClickContext ctx = c.newInstance((Object) null, 0, it);
        assertEquals(0, ctx.getSlot());
        assertSame(it, ctx.getItem());
        assertNull(ctx.getPlayer());
    }
}
