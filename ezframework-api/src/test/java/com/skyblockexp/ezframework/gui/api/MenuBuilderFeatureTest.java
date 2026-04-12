package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class MenuBuilderFeatureTest {

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void createReturnsNewInstance() {
        MenuBuilder b1 = MenuBuilder.create();
        MenuBuilder b2 = MenuBuilder.create();
        assertNotNull(b1);
        assertNotNull(b2);
        assertNotSame(b1, b2);
    }

    // -------------------------------------------------------------------------
    // title()
    // -------------------------------------------------------------------------

    @Test
    void titleIsReflectedInBuiltDefinition() {
        MenuDefinition def = MenuBuilder.create().title("Inventory").build();
        assertEquals("Inventory", def.getTitle());
    }

    @Test
    void nullTitleDefaultsToEmptyString() {
        MenuDefinition def = MenuBuilder.create().title(null).build();
        assertEquals("", def.getTitle());
    }

    @Test
    void titleReturnsSameBuilderForChaining() {
        MenuBuilder b = MenuBuilder.create();
        assertSame(b, b.title("test"));
    }

    // -------------------------------------------------------------------------
    // size()
    // -------------------------------------------------------------------------

    @Test
    void sizeIsReflectedInDefinition() {
        MenuDefinition def = MenuBuilder.create().size(27).build();
        assertEquals(27, def.getSize());
    }

    @Test
    void sizeReturnsSameBuilderForChaining() {
        MenuBuilder b = MenuBuilder.create();
        assertSame(b, b.size(9));
    }

    @Test
    void defaultSizeIsNine() {
        MenuDefinition def = MenuBuilder.create().build();
        assertEquals(9, def.getSize());
    }

    // -------------------------------------------------------------------------
    // item()
    // -------------------------------------------------------------------------

    @Test
    void itemIsPlacedAtCorrectSlot() {
        GuiItem item = new GuiItem("STONE", 1, "Rock", null);
        MenuDefinition def = MenuBuilder.create().item(3, item).build();
        assertSame(item, def.getItems().get(3));
    }

    @Test
    void multipleItemsPlacedAtDifferentSlots() {
        GuiItem i1 = new GuiItem("STONE", 1, "", null);
        GuiItem i2 = new GuiItem("DIAMOND", 1, "", null);
        MenuDefinition def = MenuBuilder.create().item(0, i1).item(8, i2).build();
        assertSame(i1, def.getItems().get(0));
        assertSame(i2, def.getItems().get(8));
    }

    @Test
    void itemReturnsSameBuilderForChaining() {
        MenuBuilder b = MenuBuilder.create();
        assertSame(b, b.item(0, new GuiItem("STONE", 1, "", null)));
    }

    // -------------------------------------------------------------------------
    // onClick()
    // -------------------------------------------------------------------------

    @Test
    void onClickHandlerIsInDefinition() {
        AtomicBoolean fired = new AtomicBoolean(false);
        MenuDefinition def = MenuBuilder.create().onClick(ctx -> fired.set(true)).build();
        assertNotNull(def.getClickHandler());
    }

    @Test
    void onClickHandlerExecutesWhenInvoked() {
        AtomicBoolean fired = new AtomicBoolean(false);
        MenuDefinition def = MenuBuilder.create().onClick(ctx -> fired.set(true)).build();
        def.getClickHandler().accept(null);
        assertTrue(fired.get());
    }

    @Test
    void onClickNullHandlerResultsInNullInDefinition() {
        MenuDefinition def = MenuBuilder.create().onClick(null).build();
        assertNull(def.getClickHandler());
    }

    @Test
    void getClickHandlerReturnsSameConsumer() {
        java.util.function.Consumer<GuiClickContext> h = ctx -> {};
        MenuBuilder b = MenuBuilder.create().onClick(h);
        assertSame(h, b.getClickHandler());
    }

    // -------------------------------------------------------------------------
    // action()
    // -------------------------------------------------------------------------

    @Test
    void actionBoundToSlotIsInDefinition() {
        GuiAction action = GuiAction.of(ctx -> {});
        MenuDefinition def = MenuBuilder.create().action(2, action).build();
        assertNotNull(def.getActions().get(2));
    }

    @Test
    void nullActionIsReplacedWithNoop() {
        MenuDefinition def = MenuBuilder.create().action(0, null).build();
        // null action is stored as noop — should be non-null
        assertNotNull(def.getActions().get(0));
    }

    @Test
    void actionReturnsSameBuilderForChaining() {
        MenuBuilder b = MenuBuilder.create();
        assertSame(b, b.action(0, GuiAction.noop()));
    }

    @Test
    void getActionsReflectsAllBoundActions() {
        GuiAction a1 = GuiAction.noop();
        GuiAction a2 = GuiAction.noop();
        MenuBuilder b = MenuBuilder.create().action(1, a1).action(5, a2);
        assertTrue(b.getActions().containsKey(1));
        assertTrue(b.getActions().containsKey(5));
    }

    // -------------------------------------------------------------------------
    // build()
    // -------------------------------------------------------------------------

    @Test
    void buildCreatesCompleteDefinition() {
        GuiItem item = new GuiItem("DIAMOND", 1, "Gem", null);
        GuiAction action = GuiAction.noop();
        MenuDefinition def = MenuBuilder.create()
                .title("Shop")
                .size(54)
                .item(0, item)
                .action(0, action)
                .onClick(ctx -> {})
                .build();

        assertEquals("Shop", def.getTitle());
        assertEquals(54, def.getSize());
        assertSame(item, def.getItems().get(0));
        assertNotNull(def.getActions().get(0));
        assertNotNull(def.getClickHandler());
    }

    @Test
    void buildCanBeCalledMultipleTimes() {
        MenuBuilder b = MenuBuilder.create().title("Menu").size(9);
        MenuDefinition d1 = b.build();
        MenuDefinition d2 = b.build();
        assertEquals(d1.getTitle(), d2.getTitle());
        assertEquals(d1.getSize(), d2.getSize());
    }
}
