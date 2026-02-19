package com.skyblockexp.ezframework.cli.generator;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileGeneratorTest {
    @Test
    public void generateSubcmdFromStubCreatesFile() throws Exception {
        Path tmp = Files.createTempDirectory("fgtest");
        try {
            Path created = FileGenerator.generateFromStub("subcmd.stub", "com.example.test", "MySub", tmp);
            assertTrue(Files.exists(created));
            String content = Files.readString(created);
            assertTrue(content.contains("class MySub"));
            assertTrue(content.contains("package com.example.test") || content.contains("package"));
        } finally {
            // best-effort cleanup
            Files.walk(tmp)
                    .sorted((a,b) -> b.compareTo(a))
                    .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
        }
    }
}
