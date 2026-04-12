package com.skyblockexp.ezframework.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigRegistryFeatureTest {

    private ConfigRegistry registry;

    @BeforeEach
    void setup() {
        registry = new ConfigRegistry();
        // Clear static default to avoid test interference
        ConfigRegistry.setDefault(null);
    }

    // -------------------------------------------------------------------------
    // register() / get()
    // -------------------------------------------------------------------------

    @Test
    void registerAndGetReturnsSameConfig() {
        EzConfig cfg = fakeConfig("config.yml", "/data/plugin");
        registry.register("config.yml", cfg);
        Optional<EzConfig> found = registry.get("config.yml");
        assertTrue(found.isPresent());
        assertSame(cfg, found.get());
    }

    @Test
    void getReturnsEmptyForUnregisteredName() {
        assertFalse(registry.get("missing.yml").isPresent());
    }

    @Test
    void registerTwoConfigsForSameNameDifferentFolders() {
        EzConfig cfg1 = fakeConfig("config.yml", "/data/pluginA");
        EzConfig cfg2 = fakeConfig("config.yml", "/data/pluginB");
        registry.register("config.yml", cfg1);
        registry.register("config.yml", cfg2);
        // get() returns one arbitrarily
        assertTrue(registry.get("config.yml").isPresent());
    }

    // -------------------------------------------------------------------------
    // getAll()
    // -------------------------------------------------------------------------

    @Test
    void getAllReturnsAllConfigsForName() {
        EzConfig cfg1 = fakeConfig("config.yml", "/data/pluginA");
        EzConfig cfg2 = fakeConfig("config.yml", "/data/pluginB");
        registry.register("config.yml", cfg1);
        registry.register("config.yml", cfg2);
        Collection<EzConfig> all = registry.getAll("config.yml");
        assertEquals(2, all.size());
        assertTrue(all.contains(cfg1));
        assertTrue(all.contains(cfg2));
    }

    @Test
    void getAllReturnsEmptyForUnknownName() {
        assertTrue(registry.getAll("notexist.yml").isEmpty());
    }

    @Test
    void getAllIsUnmodifiable() {
        EzConfig cfg = fakeConfig("config.yml", "/data/plugin");
        registry.register("config.yml", cfg);
        Collection<EzConfig> all = registry.getAll("config.yml");
        assertThrows(UnsupportedOperationException.class, () -> all.clear());
    }

    // -------------------------------------------------------------------------
    // list()
    // -------------------------------------------------------------------------

    @Test
    void listReturnsAllRegisteredConfigs() {
        EzConfig cfg1 = fakeConfig("a.yml", "/data/p1");
        EzConfig cfg2 = fakeConfig("b.yml", "/data/p2");
        registry.register("a.yml", cfg1);
        registry.register("b.yml", cfg2);
        Collection<EzConfig> list = registry.list();
        assertEquals(2, list.size());
        assertTrue(list.contains(cfg1));
        assertTrue(list.contains(cfg2));
    }

    @Test
    void listIsUnmodifiable() {
        registry.register("cfg.yml", fakeConfig("cfg.yml", "/data/p"));
        assertThrows(UnsupportedOperationException.class, () -> registry.list().clear());
    }

    @Test
    void listEmptyWhenNothingRegistered() {
        assertTrue(registry.list().isEmpty());
    }

    // -------------------------------------------------------------------------
    // unregister(name)
    // -------------------------------------------------------------------------

    @Test
    void unregisterByNameRemovesAllUnderThatName() {
        registry.register("config.yml", fakeConfig("config.yml", "/data/p1"));
        registry.register("config.yml", fakeConfig("config.yml", "/data/p2"));
        registry.unregister("config.yml");
        assertFalse(registry.get("config.yml").isPresent());
        assertTrue(registry.getAll("config.yml").isEmpty());
    }

    @Test
    void unregisterByNameDoesNothingForUnknownName() {
        assertDoesNotThrow(() -> registry.unregister("notexist.yml"));
    }

    // -------------------------------------------------------------------------
    // unregister(name, config)
    // -------------------------------------------------------------------------

    @Test
    void unregisterSpecificConfigLeavesOtherConfigs() {
        EzConfig cfg1 = fakeConfig("config.yml", "/data/pluginA");
        EzConfig cfg2 = fakeConfig("config.yml", "/data/pluginB");
        registry.register("config.yml", cfg1);
        registry.register("config.yml", cfg2);
        registry.unregister("config.yml", cfg1);
        Collection<EzConfig> remaining = registry.getAll("config.yml");
        assertEquals(1, remaining.size());
        assertTrue(remaining.contains(cfg2));
        assertFalse(remaining.contains(cfg1));
    }

    @Test
    void unregisterSpecificConfigRemovesEntryWhenLastOne() {
        EzConfig cfg = fakeConfig("config.yml", "/data/plugin");
        registry.register("config.yml", cfg);
        registry.unregister("config.yml", cfg);
        assertFalse(registry.get("config.yml").isPresent());
        assertTrue(registry.getAll("config.yml").isEmpty());
    }

    @Test
    void unregisterSpecificConfigDoesNothingForUnknownName() {
        EzConfig cfg = fakeConfig("config.yml", "/data/plugin");
        assertDoesNotThrow(() -> registry.unregister("notexist.yml", cfg));
    }

    // -------------------------------------------------------------------------
    // clear()
    // -------------------------------------------------------------------------

    @Test
    void clearRemovesAllEntries() {
        registry.register("a.yml", fakeConfig("a.yml", "/a"));
        registry.register("b.yml", fakeConfig("b.yml", "/b"));
        registry.clear();
        assertTrue(registry.list().isEmpty());
    }

    // -------------------------------------------------------------------------
    // setDefault() / getDefault()
    // -------------------------------------------------------------------------

    @Test
    void getDefaultNullWhenNotSet() {
        ConfigRegistry.setDefault(null);
        assertNull(ConfigRegistry.getDefault());
    }

    @Test
    void setDefaultMakesInstanceRetrievable() {
        ConfigRegistry.setDefault(registry);
        assertSame(registry, ConfigRegistry.getDefault());
    }

    @Test
    void setDefaultNullClearsDefault() {
        ConfigRegistry.setDefault(registry);
        ConfigRegistry.setDefault(null);
        assertNull(ConfigRegistry.getDefault());
    }

    @Test
    void setDefaultWithDifferentInstances() {
        ConfigRegistry r1 = new ConfigRegistry();
        ConfigRegistry r2 = new ConfigRegistry();
        ConfigRegistry.setDefault(r1);
        assertSame(r1, ConfigRegistry.getDefault());
        ConfigRegistry.setDefault(r2);
        assertSame(r2, ConfigRegistry.getDefault());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static EzConfig fakeConfig(String fileName, String folder) {
        return new EzConfig() {
            @Override public void load() {}
            @Override public void reload() {}
            @Override public void save() {}
            @Override public void saveDefault() {}
            @Override public Path getDataFolder() { return Paths.get(folder); }
            @Override public String getFileName() { return fileName; }
        };
    }
}
