package com.skyblockexp.ezframework.config.impl;

import com.skyblockexp.ezframework.config.impl.YamlEzConfig;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.skyblockexp.ezframework.testutil.TestPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class YamlEzConfigFeatureTest {

    private Path tmpDir;

    @AfterEach
    public void cleanup() throws IOException {
        try { MockBukkit.unmock(); } catch (Exception ignored) {}
        if (tmpDir != null && Files.exists(tmpDir)) {
            try (var s = Files.walk(tmpDir)) {
                s.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
        }
    }

    @Test
    public void saveDefault_load_and_save_roundtrip() throws Exception {
        tmpDir = Files.createTempDirectory("yamltest");
        ServerMock server = MockBukkit.mock();
        TestPlugin plugin = MockBukkit.load(TestPlugin.class);
        // set plugin data folder to our temp dir
        try {
            java.lang.reflect.Field fld = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredField("dataFolder");
            fld.setAccessible(true);
            fld.set(plugin, tmpDir.toFile());
        } catch (Throwable t) {
            throw new RuntimeException("Failed to set dataFolder on TestPlugin", t);
        }

        YamlEzConfig cfg = new YamlEzConfig(plugin, "config.yml");
        // copy default from test resources
        cfg.saveDefault();
        Assertions.assertTrue(Files.exists(tmpDir.resolve("config.yml")), "config.yml should be copied to data folder");

        cfg.load();
        FileConfiguration fc = cfg.getConfig();
        Assertions.assertEquals("hello", fc.getString("some.value"));

        // modify and save
        fc.set("some.value", "world");
        cfg.save();

        // reload from disk and verify
        cfg.reload();
        Assertions.assertEquals("world", cfg.getConfig().getString("some.value"));
    }

    
}
