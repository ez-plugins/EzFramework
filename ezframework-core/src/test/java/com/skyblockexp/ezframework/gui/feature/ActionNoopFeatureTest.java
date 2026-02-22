package com.skyblockexp.ezframework.gui.feature;

import com.skyblockexp.ezframework.gui.api.GuiAction;
import com.skyblockexp.ezframework.gui.api.GuiClickContext;
import com.skyblockexp.ezframework.gui.api.GuiPlayer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ActionNoopFeatureTest {

    @Test
    public void noopActionDoesNotThrow() {
        GuiAction noop = GuiAction.noop();
        // should not throw
        GuiPlayer stub = new GuiPlayer() { public UUID getUniqueId() { return UUID.randomUUID(); } public String getName() { return "test"; } public void sendMessage(String m) {} };
        noop.execute(new GuiClickContext(stub, 0, null));
    }
}
