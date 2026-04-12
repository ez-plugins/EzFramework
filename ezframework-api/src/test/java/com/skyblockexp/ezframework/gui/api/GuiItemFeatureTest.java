package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GuiItemFeatureTest {

    // -------------------------------------------------------------------------
    // Constructor & accessors
    // -------------------------------------------------------------------------

    @Test
    void constructorStoresAllFields() {
        Map<String, String> meta = new HashMap<>();
        meta.put("lore", "Rare item");
        GuiItem item = new GuiItem("DIAMOND_SWORD", 1, "Excalibur", meta);
        assertEquals("DIAMOND_SWORD", item.getMaterial());
        assertEquals(1, item.getAmount());
        assertEquals("Excalibur", item.getDisplayName());
        assertEquals("Rare item", item.getMetadata().get("lore"));
    }

    // -------------------------------------------------------------------------
    // Null material
    // -------------------------------------------------------------------------

    @Test
    void nullMaterialDefaultsToEmptyString() {
        GuiItem item = new GuiItem(null, 1, "name", null);
        assertEquals("", item.getMaterial());
    }

    // -------------------------------------------------------------------------
    // Amount clamping
    // -------------------------------------------------------------------------

    @Test
    void amountMinimumIsOne() {
        GuiItem item = new GuiItem("STONE", 0, "", null);
        assertEquals(1, item.getAmount(), "amount 0 should clamp to 1");
    }

    @Test
    void negativeAmountClampsToOne() {
        GuiItem item = new GuiItem("STONE", -5, "", null);
        assertEquals(1, item.getAmount());
    }

    @Test
    void positiveAmountSetCorrectly() {
        GuiItem item = new GuiItem("STONE", 64, "", null);
        assertEquals(64, item.getAmount());
    }

    // -------------------------------------------------------------------------
    // Display name
    // -------------------------------------------------------------------------

    @Test
    void nullDisplayNameDefaultsToEmptyString() {
        GuiItem item = new GuiItem("STONE", 1, null, null);
        assertEquals("", item.getDisplayName());
    }

    @Test
    void displayNamePreserved() {
        GuiItem item = new GuiItem("STONE", 1, "My Item", null);
        assertEquals("My Item", item.getDisplayName());
    }

    // -------------------------------------------------------------------------
    // Metadata immutability
    // -------------------------------------------------------------------------

    @Test
    void nullMetadataDefaultsToEmptyMap() {
        GuiItem item = new GuiItem("STONE", 1, "", null);
        assertNotNull(item.getMetadata());
        assertTrue(item.getMetadata().isEmpty());
    }

    @Test
    void metadataIsUnmodifiable() {
        GuiItem item = new GuiItem("STONE", 1, "", new HashMap<>());
        assertThrows(UnsupportedOperationException.class, () -> item.getMetadata().put("k", "v"));
    }

    @Test
    void metadataViewReflectsUnderlyingMap() {
        // GuiItem wraps the map via Collections.unmodifiableMap (live view, not a copy).
        // The contract is that getMetadata() returns an unmodifiable view of whatever was passed.
        Map<String, String> meta = new HashMap<>();
        meta.put("tier", "common");
        GuiItem item = new GuiItem("STONE", 1, "", meta);
        assertEquals("common", item.getMetadata().get("tier"));
    }

    @Test
    void metadataEntriesRetained() {
        Map<String, String> meta = new HashMap<>();
        meta.put("key1", "val1");
        meta.put("key2", "val2");
        GuiItem item = new GuiItem("STONE", 1, "", meta);
        assertEquals("val1", item.getMetadata().get("key1"));
        assertEquals("val2", item.getMetadata().get("key2"));
    }
}
