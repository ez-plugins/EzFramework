package com.skyblockexp.ezframework.gui.api;

/**
 * Context passed to click handlers. Uses a platform-agnostic `GuiPlayer`.
 */
public final class GuiClickContext {
    private final GuiPlayer player;
    private final int slot;
    private final GuiItem item;

    public GuiClickContext(GuiPlayer player, int slot, GuiItem item) {
        this.player = player;
        this.slot = slot;
        this.item = item;
    }

    public GuiPlayer getPlayer() { return player; }

    public int getSlot() { return slot; }

    public GuiItem getItem() { return item; }
}
