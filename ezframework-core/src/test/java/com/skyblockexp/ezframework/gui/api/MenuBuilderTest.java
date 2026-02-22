package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class MenuBuilderTest {

    @Test
    public void buildProducesDefinition() {
        AtomicBoolean clicked = new AtomicBoolean(false);

        MenuDefinition d = MenuBuilder.create()
                .title("T")
                .size(9)
                .item(0, new GuiItem("STONE", 1, "Stone", null))
                .onClick(ctx -> clicked.set(true))
                .action(0, GuiAction.of(ctx -> clicked.set(true)))
                .build();

        assertEquals("T", d.getTitle());
        assertEquals(9, d.getSize());
        assertTrue(d.getItems().containsKey(0));
        assertNotNull(d.getClickHandler());
        assertTrue(d.getActions().containsKey(0));
    }
}
