package com.skyblockexp.ezframework.config;

import com.skyblockexp.ezframework.config.impl.YamlEzConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.skyblockexp.ezframework.testutil.TestPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigBootstrapTest {
    private Path tmpDir;

    @AfterEach
    public void cleanup() throws Exception {
        try { MockBukkit.unmock(); } catch (Exception ignored) {}
        if (tmpDir != null && Files.exists(tmpDir)) {
            try (var s = Files.walk(tmpDir)) {
                s.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }

    @Test
    public void start_appliesIndexedMigrations_and_savesDefaultConfig() throws Exception {
        tmpDir = Files.createTempDirectory("cfgboot");

        ServerMock server = MockBukkit.mock();
        TestPlugin plugin = MockBukkit.load(TestPlugin.class);

        // set plugin data folder to temp
        Field fld = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredField("dataFolder");
        fld.setAccessible(true);
        fld.set(plugin, tmpDir.toFile());

        // ensure there is a default config resource in test resources (config.yml)
        ConfigBootstrap bs = new ConfigBootstrap(plugin, "config.yml");
        bs.start();

        // migration should have produced marker file from TestConfigMigration
        Assertions.assertTrue(Files.exists(tmpDir.resolve("applied-test-cfg-mig.txt")));

        // config.yml should have been copied by saveDefault
        Assertions.assertTrue(Files.exists(tmpDir.resolve("config.yml")));
    }

    
}
