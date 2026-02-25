package com.skyblockexp.ezframework.query;

import org.mockbukkit.mockbukkit.MockBukkit;
import com.skyblockexp.ezframework.testutil.TestPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PluginFeatureTest {

    

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
