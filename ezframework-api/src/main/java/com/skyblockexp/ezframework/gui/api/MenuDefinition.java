package com.skyblockexp.ezframework.gui.api;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.Objects;
import java.util.HashMap;

import com.skyblockexp.ezframework.gui.api.GuiClickContext;

/**
 * Lightweight, server-agnostic menu definition. Implementations convert
 * this into concrete server UI objects.
 */
public final class MenuDefinition {
    private final String title;
    private final int size;
    private final Map<Integer, GuiItem> items;
    private final Consumer<GuiClickContext> clickHandler;
    private final Map<Integer, GuiAction> actions;

    /**
     * Construct a simple menu definition with no click handler or actions.
     *
     * @param title menu title
     * @param size number of slots
     * @param items slot->item map
     */
    public MenuDefinition(String title, int size, Map<Integer, GuiItem> items) {
        this(title, size, items, null, null);
    }

    /**
     * Construct a menu definition with an optional click handler.
     *
     * @param title       menu title
     * @param size        number of slots
     * @param items       slot->item map
     * @param clickHandler global click handler (may be null)
     */
    public MenuDefinition(String title, int size, Map<Integer, GuiItem> items, Consumer<GuiClickContext> clickHandler) {
        this(title, size, items, clickHandler, null);
    }

    /**
     * Construct a menu definition with an optional click handler and actions.
     *
     * @param title       menu title
     * @param size        number of slots
     * @param items       slot->item map
     * @param clickHandler global click handler (may be null)
     * @param actions     slot->action map (may be null)
     */
    public MenuDefinition(String title, int size, Map<Integer, GuiItem> items, Consumer<GuiClickContext> clickHandler, Map<Integer, GuiAction> actions) {
        this.title = title == null ? "" : title;
        this.size = Math.max(0, size);
        this.items = (items == null) ? Collections.emptyMap() : Collections.unmodifiableMap(items);
        this.clickHandler = clickHandler;
        if (actions == null) {
            this.actions = Collections.emptyMap();
        } else {
            Map<Integer, GuiAction> copy = new HashMap<>(actions);
            this.actions = Collections.unmodifiableMap(copy);
        }
    }
    /**
     * Get the menu title.
     *
     * @return menu title
     */
    public String getTitle() { return title; }

    /**
     * Get the number of slots in the menu.
     *
     * @return menu size (slot count)
     */
    public int getSize() { return size; }

    /**
     * Get the immutable mapping of slot -> item for this menu.
     *
     * @return slot->item map (unmodifiable)
     */
    public Map<Integer, GuiItem> getItems() { return items; }

    /**
     * Get the optional global click handler for unbound slots.
     *
     * @return configured click handler or null
     */
    public Consumer<GuiClickContext> getClickHandler() { return clickHandler; }

    /**
     * Get the immutable mapping of slot -> action for this menu.
     *
     * @return slot->action map (unmodifiable)
     */
    public Map<Integer, GuiAction> getActions() { return actions; }
}
