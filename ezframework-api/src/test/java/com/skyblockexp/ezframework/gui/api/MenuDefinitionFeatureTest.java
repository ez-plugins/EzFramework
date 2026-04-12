package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MenuDefinitionFeatureTest {

    // -------------------------------------------------------------------------
    // 3-arg constructor
    // -------------------------------------------------------------------------

    @Test
    void threeArgConstructorSetsFieldsWithNullHandlerAndActions() {
        GuiItem item = new GuiItem("STONE", 1, "Rock", null);
        Map<Integer, GuiItem> items = new HashMap<>();
        items.put(0, item);
        MenuDefinition def = new MenuDefinition("Title", 9, items);

        assertEquals("Title", def.getTitle());
        assertEquals(9, def.getSize());
        assertSame(item, def.getItems().get(0));
        assertNull(def.getClickHandler());
        assertTrue(def.getActions().isEmpty());
    }

    // -------------------------------------------------------------------------
    // 4-arg constructor
    // -------------------------------------------------------------------------

    @Test
    void fourArgConstructorWithClickHandler() {
        java.util.function.Consumer<GuiClickContext> handler = ctx -> {};
        MenuDefinition def = new MenuDefinition("Title", 9, Collections.emptyMap(), handler);
        assertSame(handler, def.getClickHandler());
        assertTrue(def.getActions().isEmpty());
    }

    @Test
    void fourArgConstructorNullHandlerAllowed() {
        MenuDefinition def = new MenuDefinition("Title", 9, Collections.emptyMap(), null);
        assertNull(def.getClickHandler());
    }

    // -------------------------------------------------------------------------
    // 5-arg constructor
    // -------------------------------------------------------------------------

    @Test
    void fiveArgConstructorWithHandlerAndActions() {
        GuiAction action = GuiAction.noop();
        Map<Integer, GuiAction> actions = new HashMap<>();
        actions.put(3, action);
        java.util.function.Consumer<GuiClickContext> handler = ctx -> {};
        MenuDefinition def = new MenuDefinition("Title", 9, Collections.emptyMap(), handler, actions);
        assertSame(handler, def.getClickHandler());
        assertEquals(action, def.getActions().get(3));
    }

    @Test
    void fiveArgConstructorNullActionsDefaultsToEmptyMap() {
        MenuDefinition def = new MenuDefinition("Title", 9, Collections.emptyMap(), null, null);
        assertNotNull(def.getActions());
        assertTrue(def.getActions().isEmpty());
    }

    @Test
    void fiveArgConstructorNullItemsDefaultsToEmptyMap() {
        MenuDefinition def = new MenuDefinition("Title", 9, null, null, null);
        assertNotNull(def.getItems());
        assertTrue(def.getItems().isEmpty());
    }

    // -------------------------------------------------------------------------
    // getTitle()
    // -------------------------------------------------------------------------

    @Test
    void nullTitleDefaultsToEmptyString() {
        MenuDefinition def = new MenuDefinition(null, 9, null);
        assertEquals("", def.getTitle());
    }

    @Test
    void titlePreserved() {
        assertEquals("Shop", new MenuDefinition("Shop", 9, null).getTitle());
    }

    // -------------------------------------------------------------------------
    // getSize()
    // -------------------------------------------------------------------------

    @Test
    void sizeStoredAsGiven() {
        assertEquals(54, new MenuDefinition("", 54, null).getSize());
    }

    @Test
    void negativeSizeClampsToZero() {
        // Math.max(0, size) is used in ctor
        assertEquals(0, new MenuDefinition("", -1, null).getSize());
    }

    // -------------------------------------------------------------------------
    // getItems() immutability
    // -------------------------------------------------------------------------

    @Test
    void itemsMapIsUnmodifiable() {
        Map<Integer, GuiItem> items = new HashMap<>();
        items.put(0, new GuiItem("STONE", 1, "", null));
        MenuDefinition def = new MenuDefinition("", 9, items);
        assertThrows(UnsupportedOperationException.class, () -> def.getItems().clear());
    }

    // -------------------------------------------------------------------------
    // getActions() immutability
    // -------------------------------------------------------------------------

    @Test
    void actionsMapIsUnmodifiable() {
        Map<Integer, GuiAction> actions = new HashMap<>();
        actions.put(0, GuiAction.noop());
        MenuDefinition def = new MenuDefinition("", 9, null, null, actions);
        assertThrows(UnsupportedOperationException.class, () -> def.getActions().clear());
    }

    @Test
    void originalActionsMapChangeDoesNotAffectDefinition() {
        Map<Integer, GuiAction> actions = new HashMap<>();
        actions.put(0, GuiAction.noop());
        MenuDefinition def = new MenuDefinition("", 9, null, null, actions);
        actions.put(1, GuiAction.noop()); // mutate original
        assertFalse(def.getActions().containsKey(1), "definition must hold a snapshot of actions");
    }
}
