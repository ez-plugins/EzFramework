package com.skyblockexp.ezframework.gui.impl;

import com.skyblockexp.ezframework.gui.api.GuiItem;
import com.skyblockexp.ezframework.gui.api.MenuBuilder;
import com.skyblockexp.ezframework.gui.api.MenuDefinition;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ItemConverterTest {

    public static class TestPlugin extends org.bukkit.plugin.java.JavaPlugin {}

    @Test
    public void toAndFromItemStack_preservesDisplayAndMetadata() {
        ServerMock server = MockBukkit.mock();
        TestPlugin plugin = MockBukkit.load(TestPlugin.class);

        GuiItem item = new GuiItem("STONE", 2, "&aHello", Map.of("k=1", "v;2\\"));
        MenuDefinition menu = MenuBuilder.create().size(9).build();

        ItemStack is = ItemConverter.toItemStack(item, menu, 0, plugin);
        assertNotNull(is);
        assertEquals(2, is.getAmount());

        GuiItem converted = ItemConverter.fromItemStack(is, plugin);
        assertNotNull(converted);
        assertEquals("STONE", converted.getMaterial());
        assertEquals(2, converted.getAmount());
        // display name should be formatted via Messaging -> legacy color codes
        assertTrue(converted.getDisplayName().contains("Hello"));
        assertEquals("v;2\\", converted.getMetadata().get("k=1"));

        MockBukkit.unmock();
    }
}
