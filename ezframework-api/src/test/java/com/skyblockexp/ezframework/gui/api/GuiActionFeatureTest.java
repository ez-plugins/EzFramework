package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class GuiActionFeatureTest {

    // -------------------------------------------------------------------------
    // GuiAction.of()
    // -------------------------------------------------------------------------

    @Test
    void ofWrapsConsumerAndCallsExecute() {
        AtomicBoolean called = new AtomicBoolean(false);
        GuiAction action = GuiAction.of(ctx -> called.set(true));
        action.execute(stubContext());
        assertTrue(called.get());
    }

    @Test
    void ofPassesContextToConsumer() {
        AtomicReference<GuiClickContext> captured = new AtomicReference<>();
        GuiAction action = GuiAction.of(captured::set);
        GuiClickContext ctx = stubContext();
        action.execute(ctx);
        assertSame(ctx, captured.get());
    }

    @Test
    void ofRejectsNullConsumer() {
        assertThrows(NullPointerException.class, () -> GuiAction.of(null));
    }

    // -------------------------------------------------------------------------
    // GuiAction.noop()
    // -------------------------------------------------------------------------

    @Test
    void noopDoesNotThrow() {
        GuiAction noop = GuiAction.noop();
        assertDoesNotThrow(() -> noop.execute(stubContext()));
    }

    @Test
    void noopDoesNothingObservable() {
        AtomicBoolean changed = new AtomicBoolean(false);
        // if noop doesn't call anything, changed stays false
        GuiAction.noop().execute(stubContext());
        assertFalse(changed.get());
    }

    @Test
    void noopCanBeCalledRepeatedly() {
        GuiAction noop = GuiAction.noop();
        assertDoesNotThrow(() -> {
            noop.execute(stubContext());
            noop.execute(stubContext());
            noop.execute(null); // even null context shouldn't throw (noop ignores it)
        });
    }

    // -------------------------------------------------------------------------
    // execute() on lambda
    // -------------------------------------------------------------------------

    @Test
    void executeCanReceiveNullContextIfConsumerAllows() {
        GuiAction action = GuiAction.of(ctx -> {}); // ignore ctx
        assertDoesNotThrow(() -> action.execute(null));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static GuiClickContext stubContext() {
        GuiPlayer player = new GuiPlayer() {
            @Override public java.util.UUID getUniqueId() { return java.util.UUID.randomUUID(); }
            @Override public String getName() { return "TestPlayer"; }
            @Override public void sendMessage(String message) {}
        };
        return new GuiClickContext(player, 0, null);
    }
}
