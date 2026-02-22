package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GuiActionTest {

    @Test
    public void guiActionExecutes() {
        AtomicBoolean ran = new AtomicBoolean(false);
        GuiAction a = GuiAction.of(ctx -> ran.set(true));
        a.execute(null);
        assertTrue(ran.get());
    }

    @Test
    public void noopDoesNothing() {
        GuiAction.noop().execute(null);
    }
}
