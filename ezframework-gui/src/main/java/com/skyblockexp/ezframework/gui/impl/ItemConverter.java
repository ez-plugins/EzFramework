package com.skyblockexp.ezframework.gui.impl;

import com.skyblockexp.ezframework.gui.api.GuiItem;
import com.skyblockexp.ezframework.gui.api.MenuDefinition;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.skyblockexp.ezframework.message.api.Messaging;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Responsibility: translate between core `GuiItem` and Bukkit `ItemStack`,
 * including storing hidden metadata and action ids in the PersistentDataContainer.
 */
final class ItemConverter {
    private static final String META_KEY = "ez_meta";
    private static final String ACTION_KEY = "ez_action";

    private ItemConverter() {}

    static ItemStack toItemStack(GuiItem item, MenuDefinition menu, int slot, JavaPlugin plugin) {
        Material m = Material.STONE;
        if (item.getMaterial() != null) {
            try {
                m = Material.valueOf(item.getMaterial().toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                m = Material.STONE;
            }
        }
        ItemStack is = new ItemStack(m, Math.max(1, item.getAmount()));
        ItemMeta meta = is.getItemMeta();
        if (meta != null && item.getDisplayName() != null && !item.getDisplayName().isEmpty()) {
            try {
                String formatted = Messaging.forPlugin(plugin).format(item.getDisplayName());
                // Use legacy string display name; fallback to component APIs avoided to keep
                // dependency on Adventure optional via the MessageProvider.
                meta.setDisplayName(formatted);
            } catch (Throwable ignored) {
            }
        }

        if (meta != null && plugin != null) {
            try {
                NamespacedKey metaKey = new NamespacedKey(plugin, META_KEY);
                String serialized = MetadataSerializer.serializeMetadata(item.getMetadata());
                if (!serialized.isEmpty()) {
                    meta.getPersistentDataContainer().set(metaKey, PersistentDataType.STRING, serialized);
                }

                if (menu != null && menu.getActions() != null && menu.getActions().containsKey(slot)) {
                    NamespacedKey actionKey = new NamespacedKey(plugin, ACTION_KEY);
                    meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, Integer.toString(slot));
                }
            } catch (Throwable ignored) {
            }
            is.setItemMeta(meta);
        }

        return is;
    }

    static GuiItem fromItemStack(ItemStack is, JavaPlugin plugin) {
        if (is == null) return null;
        String name = is.getType().name();
        String display = "";
        if (is.hasItemMeta()) {
            ItemMeta im = is.getItemMeta();
            try {
                if (im.hasDisplayName()) {
                    // Prefer the string display name if present. This may be provided
                    // by the Messaging provider as legacy color codes.
                    try {
                        display = im.getDisplayName();
                    } catch (Throwable t) {
                        // Older/Newer API mismatch: fall back to component-based read if available
                        try {
                            Component comp = im.displayName();
                            if (comp != null) display = LegacyComponentSerializer.legacyAmpersand().serialize(comp);
                        } catch (Throwable ignored) {
                        }
                    }
                }
            } catch (Throwable ignored) {
                // unable to read as Component, leave display empty
            }
        }
        java.util.Map<String, String> metadata = java.util.Collections.emptyMap();

        if (is.hasItemMeta() && plugin != null) {
            try {
                ItemMeta im = is.getItemMeta();
                NamespacedKey metaKey = new NamespacedKey(plugin, META_KEY);
                if (im.getPersistentDataContainer().has(metaKey, PersistentDataType.STRING)) {
                    String serialized = im.getPersistentDataContainer().get(metaKey, PersistentDataType.STRING);
                    metadata = MetadataSerializer.parseMetadata(serialized);
                }
            } catch (Throwable ignored) {
                metadata = java.util.Collections.emptyMap();
            }
        }

        return new GuiItem(name, is.getAmount(), display, metadata);
    }
}
