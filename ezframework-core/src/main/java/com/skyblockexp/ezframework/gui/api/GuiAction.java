package com.skyblockexp.ezframework.gui.api;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an action bound to a GUI slot. Implementations should
 * perform whatever behavior is desired when the slot is clicked.
 */
@FunctionalInterface
public interface GuiAction {
    void execute(GuiClickContext ctx);

    static GuiAction of(Consumer<GuiClickContext> c) {
        Objects.requireNonNull(c, "consumer");
        return c::accept;
    }

    static GuiAction noop() {
        return ctx -> {};
    }
}
