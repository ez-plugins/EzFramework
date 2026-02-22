package com.skyblockexp.ezframework.gui.feature;

import com.skyblockexp.ezframework.gui.api.GuiClickContext;
import com.skyblockexp.ezframework.gui.api.GuiItem;
import com.skyblockexp.ezframework.gui.api.MenuBuilder;
import com.skyblockexp.ezframework.gui.api.MenuDefinition;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import com.skyblockexp.ezframework.gui.api.GuiPlayer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FallbackToMenuHandlerFeatureTest {

    @Test
    public void missingActionFallsBackToMenuHandler() {
        AtomicBoolean menuRan = new AtomicBoolean(false);

        MenuDefinition menu = MenuBuilder.create()
                .title("Feature")
                .size(9)
                .item(0, new GuiItem("STONE", 1, "Stone", null))
                .onClick(ctx -> menuRan.set(true))
                .build();

        GuiPlayer stub = new GuiPlayer() { public UUID getUniqueId() { return UUID.randomUUID(); } public String getName() { return "test"; } public void sendMessage(String m) {} };
        GuiClickContext ctx = new GuiClickContext(stub, 0, menu.getItems().get(0));
        if (menu.getActions().get(0) != null) {
            menu.getActions().get(0).execute(ctx);
        } else if (menu.getClickHandler() != null) {
            menu.getClickHandler().accept(ctx);
        }

        assertTrue(menuRan.get());
    }
}
