package com.skyblockexp.ezframework.module;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleDescriptorFeatureTest {

    // -------------------------------------------------------------------------
    // Constructor happy path
    // -------------------------------------------------------------------------

    @Test
    void constructorStoresPluginName() {
        ModuleDescriptor d = new ModuleDescriptor("MyPlugin", List.of("core"), Map.of());
        assertEquals("MyPlugin", d.pluginName());
    }

    @Test
    void constructorStoresModules() {
        ModuleDescriptor d = new ModuleDescriptor("P", List.of("core", "mysql"), Map.of());
        assertEquals(List.of("core", "mysql"), d.modules());
    }

    @Test
    void constructorStoresOverrides() {
        ModuleDescriptor d = new ModuleDescriptor("P", List.of(), Map.of("k", "v"));
        assertEquals("v", d.overrides().get("k"));
    }

    @Test
    void emptyModulesAndOverridesAreAllowed() {
        assertDoesNotThrow(() -> new ModuleDescriptor("P", List.of(), Map.of()));
    }

    // -------------------------------------------------------------------------
    // Null rejection
    // -------------------------------------------------------------------------

    @Test
    void nullPluginNameThrowsNpe() {
        assertThrows(NullPointerException.class,
                () -> new ModuleDescriptor(null, List.of(), Map.of()));
    }

    @Test
    void nullModulesThrowsNpe() {
        assertThrows(NullPointerException.class,
                () -> new ModuleDescriptor("P", null, Map.of()));
    }

    @Test
    void nullOverridesThrowsNpe() {
        assertThrows(NullPointerException.class,
                () -> new ModuleDescriptor("P", List.of(), null));
    }

    // -------------------------------------------------------------------------
    // Immutability of returned collections
    // -------------------------------------------------------------------------

    @Test
    void modulesListIsUnmodifiable() {
        ModuleDescriptor d = new ModuleDescriptor("P", List.of("core"), Map.of());
        assertThrows(UnsupportedOperationException.class, () -> d.modules().add("extra"));
    }

    @Test
    void overridesMapIsUnmodifiable() {
        ModuleDescriptor d = new ModuleDescriptor("P", List.of(), Map.of("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> d.overrides().put("x", "y"));
    }

    // -------------------------------------------------------------------------
    // Snapshot safety — mutating originals does NOT affect the descriptor
    // -------------------------------------------------------------------------

    @Test
    void mutatingOriginalListDoesNotAffectDescriptor() {
        List<String> mods = new ArrayList<>();
        mods.add("core");
        ModuleDescriptor d = new ModuleDescriptor("P", mods, Map.of());

        mods.add("mysql");

        assertEquals(1, d.modules().size(), "descriptor should hold a copy of the original list");
    }

    @Test
    void mutatingOriginalMapDoesNotAffectDescriptor() {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("k", "v");
        ModuleDescriptor d = new ModuleDescriptor("P", List.of(), overrides);

        overrides.put("extra", "x");

        assertFalse(d.overrides().containsKey("extra"), "descriptor should hold a copy of the original map");
    }

    // -------------------------------------------------------------------------
    // toString sanity
    // -------------------------------------------------------------------------

    @Test
    void toStringContainsPluginName() {
        ModuleDescriptor d = new ModuleDescriptor("AwesomePlugin", List.of("core"), Map.of());
        assertTrue(d.toString().contains("AwesomePlugin"));
    }

    @Test
    void toStringContainsModuleEntries() {
        ModuleDescriptor d = new ModuleDescriptor("P", List.of("core", "mysql"), Map.of());
        String s = d.toString();
        assertTrue(s.contains("core") && s.contains("mysql"));
    }
}
