package com.skyblockexp.ezframework.cli.generator;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StubLoaderTest {
    @Test
    public void loadRepoStub() throws IOException {
        String content = StubLoader.load("repo.stub");
        assertNotNull(content);
        assertTrue(content.contains("AbstractRepository") || content.contains("{{CLASS}}"));
    }
}
