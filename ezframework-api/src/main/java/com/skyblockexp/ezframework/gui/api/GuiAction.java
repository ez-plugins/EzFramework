package com.skyblockexp.ezframework.gui.api;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an action bound to a GUI slot. Implementations should
 * perform whatever behavior is desired when the slot is clicked.
 */
@FunctionalInterface
public interface GuiAction {
    /**
     * Execute the action for the given click context.
     * @param ctx click context
     */
    void execute(GuiClickContext ctx);

    /**
     * Create a {@link GuiAction} from a consumer.
     * @param c consumer that accepts the click context
     * @return a GuiAction delegating to the consumer
     */
    static GuiAction of(Consumer<GuiClickContext> c) {
        Objects.requireNonNull(c, "consumer");
        return c::accept;
    }

    /**
     * No-op action that does nothing.
     * @return a noop action
     */
    static GuiAction noop() {
        return ctx -> {};
    }
}
