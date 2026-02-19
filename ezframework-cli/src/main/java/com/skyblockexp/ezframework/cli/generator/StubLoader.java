package com.skyblockexp.ezframework.cli.generator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class StubLoader {
    private StubLoader() {}

    public static String load(String name) throws IOException {
        try (InputStream is = StubLoader.class.getResourceAsStream("/stubs/" + name)) {
            if (is == null) throw new IOException("Stub not found: " + name);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
