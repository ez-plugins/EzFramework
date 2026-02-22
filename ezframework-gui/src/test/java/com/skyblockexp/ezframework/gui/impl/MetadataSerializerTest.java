package com.skyblockexp.ezframework.gui.impl;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataSerializerTest {

    @Test
    public void serializeAndParse_handlesEscapes() {
        Map<String, String> original = Map.of("k=;\\", "v=;\\");

        String serialized = MetadataSerializer.serializeMetadata(original);
        assertNotNull(serialized);

        var parsed = MetadataSerializer.parseMetadata(serialized);
        assertEquals(1, parsed.size());
        assertEquals("v=;\\", parsed.get("k=;\\"));
    }
}
