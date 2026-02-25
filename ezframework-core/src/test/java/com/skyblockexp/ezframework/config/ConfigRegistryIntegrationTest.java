package com.skyblockexp.ezframework.config;

import com.skyblockexp.ezframework.config.ConfigRegistry;
import com.skyblockexp.ezframework.config.EzConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigRegistryIntegrationTest {

    @AfterEach
    public void cleanup() {
        ConfigRegistry r = new ConfigRegistry();
        ConfigRegistry.setDefault(null);
    }

    @Test
    public void multiplePluginsSameFilenameDoNotOverride() throws IOException {
        ConfigRegistry registry = new ConfigRegistry();
        ConfigRegistry.setDefault(registry);

        EzConfig cfg1 = new EzConfig() {
            @Override
            public void load() throws IOException {}

            @Override
            public void reload() throws IOException {}

            @Override
            public void save() throws IOException {}

            @Override
            public void saveDefault() throws IOException {}

            @Override
            public Path getDataFolder() { return Path.of("/tmp/pluginA"); }

            @Override
            public String getFileName() { return "config.yml"; }
        };

        EzConfig cfg2 = new EzConfig() {
            @Override
            public void load() throws IOException {}

            @Override
            public void reload() throws IOException {}

            @Override
            public void save() throws IOException {}

            @Override
            public void saveDefault() throws IOException {}

            @Override
            public Path getDataFolder() { return Path.of("/tmp/pluginB"); }

            @Override
            public String getFileName() { return "config.yml"; }
        };

        registry.register(cfg1.getFileName(), cfg1);
        registry.register(cfg2.getFileName(), cfg2);

        Collection<EzConfig> all = registry.getAll("config.yml");
        assertEquals(2, all.size(), "Both plugin configs should be registered without overriding");

        Collection<EzConfig> listed = registry.list();
        assertTrue(listed.size() >= 2, "Registry list should contain both configs");

        // unregister specific should remove one
        registry.unregister("config.yml", cfg1);
        assertEquals(1, registry.getAll("config.yml").size());

        // unregister by name should remove remaining
        registry.unregister("config.yml");
        assertEquals(0, registry.getAll("config.yml").size());
    }
}
