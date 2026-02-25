package com.skyblockexp.ezframework.testutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Small helpers for temporary directories used in tests. */
public final class TempFileHelpers {
    private TempFileHelpers() {}

    public static Path createTempDir(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteRecursive(Path dir) {
        if (dir == null) return;
        try {
            Files.walk(dir)
                .sorted((a,b) -> b.compareTo(a))
                .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
        } catch (IOException ignored) {}
    }
}
