package com.skyblockexp.ezframework.gui.api;

/**
 * Context passed to click handlers. Uses a platform-agnostic `GuiPlayer`.
 */
public final class GuiClickContext {
    private final GuiPlayer player;
    private final int slot;
    private final GuiItem item;
    /**
     * Create a click context.
     * @param player player who clicked
     * @param slot clicked slot index
     * @param item item at that slot (may be null)
     */
    public GuiClickContext(GuiPlayer player, int slot, GuiItem item) {
        this.player = player;
        this.slot = slot;
        this.item = item;
    }

    /**
     * Get the player that triggered the click.
     *
     * @return the player who clicked
     */
    public GuiPlayer getPlayer() { return player; }

    /**
     * Get the clicked slot index within the menu.
     *
     * @return the clicked slot index
     */
    public int getSlot() { return slot; }

    /**
     * Get the item snapshot present in the clicked slot, if any.
     *
     * @return the item at the clicked slot (may be null)
     */
    public GuiItem getItem() { return item; }
}
