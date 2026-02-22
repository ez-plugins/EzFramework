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

    public GuiItem(String material, int amount, String displayName, Map<String, String> metadata) {
        this.material = material == null ? "" : material;
        this.amount = Math.max(1, amount);
        this.displayName = displayName == null ? "" : displayName;
        this.metadata = (metadata == null) ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
    }

    public String getMaterial() { return material; }

    public int getAmount() { return amount; }

    public String getDisplayName() { return displayName; }

    public Map<String, String> getMetadata() { return metadata; }
}
