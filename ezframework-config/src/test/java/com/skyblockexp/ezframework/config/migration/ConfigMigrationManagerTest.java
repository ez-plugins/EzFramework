package com.skyblockexp.ezframework.config.migration;

import com.skyblockexp.ezframework.config.ConfigMigration;
import com.skyblockexp.ezframework.config.EzConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ConfigMigrationManagerTest {

    private Path tmpDir;

    @AfterEach
    public void cleanup() throws IOException {
        if (tmpDir != null && Files.exists(tmpDir)) {
            try (var s = Files.walk(tmpDir)) {
                s.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
            }
        }
    }

    @Test
    public void applyMigrations_writesManifestAndRunsMigrations() throws Exception {
        tmpDir = Files.createTempDirectory("cfgtest");
        EzConfig cfg = new TempEzConfig(tmpDir, "config.yml");

        ConfigMigrationManager mgr = new ConfigMigrationManager();
        ConfigMigration m1 = new DummyMigration("mig-1");
        ConfigMigration m2 = new DummyMigration("mig-2");

        mgr.applyMigrations(cfg, List.of(m1, m2));

        Path manifest = tmpDir.resolve("applied_config_migrations.txt");
        Assertions.assertTrue(Files.exists(manifest), "manifest should exist");
        List<String> lines = Files.readAllLines(manifest, StandardCharsets.UTF_8);
        Assertions.assertTrue(lines.contains("mig-1"));
        Assertions.assertTrue(lines.contains("mig-2"));

        // check that migration side-effects ran
        Assertions.assertTrue(Files.exists(tmpDir.resolve("ran-mig-1.txt")));
        Assertions.assertTrue(Files.exists(tmpDir.resolve("ran-mig-2.txt")));
    }

    @Test
    public void applyMigrations_idempotent_skipsAlreadyApplied() throws Exception {
        tmpDir = Files.createTempDirectory("cfgtest2");
        EzConfig cfg = new TempEzConfig(tmpDir, "config.yml");

        ConfigMigrationManager mgr = new ConfigMigrationManager();
        ConfigMigration m = new DummyMigration("mig-x");

        mgr.applyMigrations(cfg, List.of(m));
        mgr.applyMigrations(cfg, List.of(m)); // second application should be skipped

        Path manifest = tmpDir.resolve("applied_config_migrations.txt");
        List<String> lines = Files.readAllLines(manifest, StandardCharsets.UTF_8);
        long count = lines.stream().filter(l -> l.equals("mig-x")).count();
        Assertions.assertEquals(1, count, "migration id should appear only once");
    }

    // Simple EzConfig test stub that uses a temp directory for the data folder
    static class TempEzConfig implements EzConfig {
        private final Path dataFolder;
        private final String fileName;

        TempEzConfig(Path dataFolder, String fileName) {
            this.dataFolder = dataFolder;
            this.fileName = fileName;
        }

        @Override
        public void load() throws IOException {
            Path f = dataFolder.resolve(fileName);
            if (!Files.exists(dataFolder)) Files.createDirectories(dataFolder);
            if (!Files.exists(f)) Files.writeString(f, "# default\n", StandardCharsets.UTF_8);
        }

        @Override
        public void reload() throws IOException {
            load();
        }

        @Override
        public void save() throws IOException {
            // no-op for test
        }

        @Override
        public void saveDefault() throws IOException {
            load();
        }

        @Override
        public Path getDataFolder() {
            return dataFolder;
        }

        @Override
        public String getFileName() {
            return fileName;
        }
    }

    // Dummy migration that writes a marker file and uses its id
    static class DummyMigration implements ConfigMigration {
        private final String id;

        DummyMigration(String id) { this.id = id; }

        @Override
        public String id() { return id; }

        @Override
        public void apply(EzConfig config) throws Exception {
            Path marker = config.getDataFolder().resolve("ran-" + id + ".txt");
            if (!Files.exists(config.getDataFolder())) Files.createDirectories(config.getDataFolder());
            Files.writeString(marker, "ok", StandardCharsets.UTF_8);
        }
    }
}
