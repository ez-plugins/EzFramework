package com.skyblockexp.ezframework.cli.generator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Helper to load text stubs from the classpath resource folder `/stubs`.
 */
public final class StubLoader {
    private StubLoader() {}

    /**
     * Load a stub text resource as a UTF-8 string.
     *
     * @param name stub file name (e.g. "repo.stub")
     * @return stub content as string
     * @throws IOException when resource is missing or cannot be read
     */
    public static String load(String name) throws IOException {
        try (InputStream is = StubLoader.class.getResourceAsStream("/stubs/" + name)) {
            if (is == null) throw new IOException("Stub not found: " + name);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
