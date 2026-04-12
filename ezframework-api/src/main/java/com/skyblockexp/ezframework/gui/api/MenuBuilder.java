package com.skyblockexp.ezframework.gui.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Fluent builder for {@link MenuDefinition}.
 * <p>
 * Use {@link #create()} to obtain a new builder instance.
 */
public final class MenuBuilder {
    private String title = "";
    private int size = 9;
    private final Map<Integer, GuiItem> items = new HashMap<>();
    private final Map<Integer, GuiAction> actions = new HashMap<>();
    private Consumer<GuiClickContext> clickHandler;

    /**
     * Create a new MenuBuilder instance.
     */
    private MenuBuilder() {}

    /**
     * Create a new builder instance.
     *
     * @return a new {@link MenuBuilder}
     */
    public static MenuBuilder create() { return new MenuBuilder(); }

    /**
     * Set the menu title.
     * @param title menu title
     * @return this builder
     */
    public MenuBuilder title(String title) { this.title = title == null ? "" : title; return this; }

    /**
     * Set the menu size (number of slots).
     * @param size slot count
     * @return this builder
     */
    public MenuBuilder size(int size) { this.size = size; return this; }

    /**
     * Place an item in a slot.
     * @param slot slot index
     * @param item item snapshot
     * @return this builder
     */
    public MenuBuilder item(int slot, GuiItem item) { this.items.put(slot, item); return this; }

    /**
     * Set a global click handler invoked for unbound slots.
     * @param handler click context consumer
     * @return this builder
     */
    public MenuBuilder onClick(Consumer<GuiClickContext> handler) { this.clickHandler = handler; return this; }

    /**
     * Bind an action to a slot.
     * @param slot slot index
     * @param action action to run when clicked
     * @return this builder
     */
    public MenuBuilder action(int slot, GuiAction action) { this.actions.put(slot, action == null ? GuiAction.noop() : action); return this; }

    /**
     * Build the immutable {@link MenuDefinition} from the current builder state.
     *
     * @return built {@link MenuDefinition}
     */
    public MenuDefinition build() { return new MenuDefinition(title, size, items, clickHandler, actions); }

    /**
     * Get the configured global click handler.
     *
     * @return configured click handler (may be null)
     */
    public Consumer<GuiClickContext> getClickHandler() { return clickHandler; }

    /**
     * Get the configured slot actions map.
     *
     * @return configured slot actions map
     */
    public Map<Integer, GuiAction> getActions() { return actions; }
}
