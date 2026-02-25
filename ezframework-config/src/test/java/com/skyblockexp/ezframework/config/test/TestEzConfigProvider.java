package com.skyblockexp.ezframework.config.test;

import com.skyblockexp.ezframework.config.EzConfig;
import com.skyblockexp.ezframework.config.EzConfigProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Test provider used in unit tests via SPI resource. Returns a minimal
 * in-process EzConfig implementation that doesn't require Bukkit.
 */
public class TestEzConfigProvider implements EzConfigProvider {
    @Override
    public EzConfig create() {
        return new EzConfig() {
            private final Path folder = Path.of(System.getProperty("java.io.tmpdir"), "ezframework-test-config");
            private final String fileName = "auto-config.yml";

            @Override
            public void load() throws java.io.IOException {
                Files.createDirectories(folder);
                Path f = folder.resolve(fileName);
                if (!Files.exists(f)) Files.writeString(f, "hello: world\n", StandardOpenOption.CREATE_NEW);
            }

            @Override
            public void reload() throws java.io.IOException { load(); }

            @Override
            public void save() throws java.io.IOException { /* noop for tests */ }

            @Override
            public void saveDefault() throws java.io.IOException { load(); }

            @Override
            public Path getDataFolder() { return folder; }

            @Override
            public String getFileName() { return fileName; }
        };
    }
}
