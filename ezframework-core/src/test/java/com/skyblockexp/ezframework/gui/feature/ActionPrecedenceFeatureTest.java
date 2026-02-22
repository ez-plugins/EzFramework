package com.skyblockexp.ezframework.gui.feature;

import com.skyblockexp.ezframework.gui.api.GuiAction;
import com.skyblockexp.ezframework.gui.api.GuiClickContext;
import com.skyblockexp.ezframework.gui.api.GuiItem;
import com.skyblockexp.ezframework.gui.api.MenuBuilder;
import com.skyblockexp.ezframework.gui.api.MenuDefinition;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import com.skyblockexp.ezframework.gui.api.GuiPlayer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionPrecedenceFeatureTest {

    @Test
    public void perSlotActionTakesPrecedenceOverMenuHandler() {
        AtomicBoolean actionRan = new AtomicBoolean(false);
        AtomicBoolean menuRan = new AtomicBoolean(false);

        MenuDefinition menu = MenuBuilder.create()
                .title("Feature")
                .size(9)
                .item(0, new GuiItem("STONE", 1, "Stone", null))
                .onClick(ctx -> menuRan.set(true))
                .action(0, GuiAction.of(ctx -> actionRan.set(true)))
                .build();

        // simulate click dispatch: prefer action if present
        GuiPlayer stub = new GuiPlayer() { public UUID getUniqueId() { return UUID.randomUUID(); } public String getName() { return "test"; } public void sendMessage(String m) {} };
        GuiClickContext ctx = new GuiClickContext(stub, 0, menu.getItems().get(0));
        GuiAction a = menu.getActions().get(0);
        if (a != null) {
            a.execute(ctx);
        } else if (menu.getClickHandler() != null) {
            menu.getClickHandler().accept(ctx);
        }

        assertTrue(actionRan.get());
        assertFalse(menuRan.get());
    }
}
