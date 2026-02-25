package com.skyblockexp.ezframework.config.test;

import com.skyblockexp.ezframework.config.ConfigMigration;
import com.skyblockexp.ezframework.config.EzConfig;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestConfigMigration implements ConfigMigration {
    @Override
    public String id() { return "test-cfg-mig"; }

    @Override
    public void apply(EzConfig config) throws Exception {
        Path marker = config.getDataFolder().resolve("applied-test-cfg-mig.txt");
        if (!Files.exists(config.getDataFolder())) Files.createDirectories(config.getDataFolder());
        Files.writeString(marker, "ok", StandardCharsets.UTF_8);
    }
}
