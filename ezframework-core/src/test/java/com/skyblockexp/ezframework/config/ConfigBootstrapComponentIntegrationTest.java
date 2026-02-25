package com.skyblockexp.ezframework.config;

import com.skyblockexp.ezframework.EzPlugin;
import com.skyblockexp.ezframework.testutil.TestPlatformProviders;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigBootstrapComponentIntegrationTest {

    static class DummyPlugin extends EzPlugin {
        @Override protected java.util.List<com.skyblockexp.ezframework.bootstrap.Component> components() { return java.util.List.of(); }
    }

    @Test
    public void bootstrapInvokesProvidersAndSavesOnStop() throws Exception {
        DummyPlugin p = new DummyPlugin();
        Object prov = TestPlatformProviders.createSavingProvider();
        com.skyblockexp.ezframework.config.PlatformConfigProvider provCast = (com.skyblockexp.ezframework.config.PlatformConfigProvider) prov;
        ConfigBootstrapComponent cbc = new ConfigBootstrapComponent(p, List.of(provCast));

        // start should register the config
        cbc.start();
        ConfigRegistry reg = ConfigRegistry.getDefault();
        assertNotNull(reg);
        assertEquals(1, reg.getAll("config.yml").size());

        // stop should call save on registered config
        cbc.stop();
        assertTrue(TestPlatformProviders.wasSaved(prov));
        assertNull(ConfigRegistry.getDefault());
    }
}
