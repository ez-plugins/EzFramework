package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GuiItemTest {

    @Test
    public void propertiesAreAccessible() {
        GuiItem it = new GuiItem("DIAMOND", 2, "Gem", null);
        assertEquals("DIAMOND", it.getMaterial());
        assertEquals(2, it.getAmount());
        assertEquals("Gem", it.getDisplayName());
        assertNotNull(it.getMetadata());
    }
}
