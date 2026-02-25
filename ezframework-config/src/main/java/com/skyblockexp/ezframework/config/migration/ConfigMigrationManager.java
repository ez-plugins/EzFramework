package com.skyblockexp.ezframework.config.migration;

import com.skyblockexp.ezframework.config.ConfigMigration;
import com.skyblockexp.ezframework.config.EzConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Very small migration runner for config migrations. It records applied migration ids
 * in `applied_config_migrations.txt` inside the config's data folder.
 */
public class ConfigMigrationManager {
    private static final String APPLIED_MANIFEST = "applied_config_migrations.txt";

    public void applyMigrations(EzConfig config, List<ConfigMigration> migrations) throws Exception {
        Path manifest = config.getDataFolder().resolve(APPLIED_MANIFEST);
        Set<String> applied = readApplied(manifest);

        List<String> toAppend = new ArrayList<>();
        for (ConfigMigration m : migrations) {
            if (applied.contains(m.id())) continue;
            m.apply(config);
            toAppend.add(m.id());
        }

        if (!toAppend.isEmpty()) {
            try {
                Files.createDirectories(manifest.getParent());
                Files.write(manifest, toAppend, StandardCharsets.UTF_8,
                        Files.exists(manifest) ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                throw new IOException("Failed to write applied migrations manifest", e);
            }
        }
    }

    private Set<String> readApplied(Path manifest) {
        try {
            if (!Files.exists(manifest)) return new HashSet<>();
            List<String> lines = Files.readAllLines(manifest, StandardCharsets.UTF_8);
            return new HashSet<>(lines);
        } catch (IOException e) {
            return new HashSet<>();
        }
    }
}
