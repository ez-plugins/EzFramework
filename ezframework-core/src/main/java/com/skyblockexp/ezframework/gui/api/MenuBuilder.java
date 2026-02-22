package com.skyblockexp.ezframework.gui.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Fluent builder for {@link MenuDefinition}.
 */
public final class MenuBuilder {
    private String title = "";
    private int size = 9;
    private final Map<Integer, GuiItem> items = new HashMap<>();
    private final Map<Integer, GuiAction> actions = new HashMap<>();
    private Consumer<GuiClickContext> clickHandler;

    public static MenuBuilder create() { return new MenuBuilder(); }

    public MenuBuilder title(String title) { this.title = title == null ? "" : title; return this; }

    public MenuBuilder size(int size) { this.size = size; return this; }

    public MenuBuilder item(int slot, GuiItem item) { this.items.put(slot, item); return this; }

    public MenuBuilder onClick(Consumer<GuiClickContext> handler) { this.clickHandler = handler; return this; }

    public MenuBuilder action(int slot, GuiAction action) { this.actions.put(slot, action == null ? GuiAction.noop() : action); return this; }

    public MenuDefinition build() { return new MenuDefinition(title, size, items, clickHandler, actions); }

    public Consumer<GuiClickContext> getClickHandler() { return clickHandler; }

    public Map<Integer, GuiAction> getActions() { return actions; }
}
