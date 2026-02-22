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

    public MenuDefinition(String title, int size, Map<Integer, GuiItem> items) {
        this(title, size, items, null, null);
    }

    public MenuDefinition(String title, int size, Map<Integer, GuiItem> items, Consumer<GuiClickContext> clickHandler) {
        this(title, size, items, clickHandler, null);
    }

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

    public String getTitle() { return title; }

    public int getSize() { return size; }

    public Map<Integer, GuiItem> getItems() { return items; }

    public Consumer<GuiClickContext> getClickHandler() { return clickHandler; }

    public Map<Integer, GuiAction> getActions() { return actions; }
}
