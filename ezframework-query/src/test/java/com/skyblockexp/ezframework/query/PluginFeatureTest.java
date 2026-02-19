package com.skyblockexp.ezframework.query;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PluginFeatureTest {

    public static class TestPlugin extends JavaPlugin {
    }

    @AfterEach
    public void teardown() {
        try {
            MockBukkit.unmock();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void pluginLoadsInMockServer() {
        var server = MockBukkit.mock();
        assertNotNull(server);

        var plugin = MockBukkit.load(TestPlugin.class);
        assertNotNull(plugin);
        assertTrue(plugin.isEnabled());
    }
}
