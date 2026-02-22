package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class MenuDefinitionTest {

    @Test
    public void gettersReturnProvidedValues() {
        var items = new HashMap<Integer, GuiItem>();
        items.put(1, new GuiItem("STONE", 1, "Stone", null));

        var actions = new HashMap<Integer, GuiAction>();
        actions.put(1, GuiAction.noop());

        MenuDefinition d = new MenuDefinition("X", 9, items, null, actions);

        assertEquals("X", d.getTitle());
        assertEquals(9, d.getSize());
        assertTrue(d.getItems().containsKey(1));
        assertTrue(d.getActions().containsKey(1));

        // verify unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> d.getItems().put(2, null));
    }
}
