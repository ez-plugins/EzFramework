package com.skyblockexp.ezframework.gui.api;

import java.util.Collections;
import java.util.Map;

/**
 * Simple item snapshot that avoids tying core types to server-specific
 * item classes. Implementations translate this snapshot into `ItemStack`
 * or equivalent.
 */
public final class GuiItem {
    private final String material; // server-agnostic name (e.g. "DIAMOND_SWORD")
    private final int amount;
    private final String displayName;
    private final Map<String, String> metadata;

    /**
     * Construct a new GUI item snapshot.
     * @param material server-agnostic material name (e.g. "DIAMOND_SWORD")
     * @param amount stack amount
     * @param displayName display name (may be empty)
     * @param metadata additional string metadata
     */
    public GuiItem(String material, int amount, String displayName, Map<String, String> metadata) {
        this.material = material == null ? "" : material;
        this.amount = Math.max(1, amount);
        this.displayName = displayName == null ? "" : displayName;
        this.metadata = (metadata == null) ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
    }

    /**
     * Get the server-agnostic material name for this item (e.g. "DIAMOND_SWORD").
     *
     * @return material name
     */
    public String getMaterial() { return material; }

    /**
     * Get the stack amount for this item snapshot.
     *
     * @return amount (always >= 1)
     */
    public int getAmount() { return amount; }

    /**
     * Get the display name for this item.
     *
     * @return display name (may be empty)
     */
    public String getDisplayName() { return displayName; }

    /**
     * Get the additional string metadata associated with this item.
     *
     * @return unmodifiable metadata map
     */
    public Map<String, String> getMetadata() { return metadata; }
}
