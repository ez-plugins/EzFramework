package com.skyblockexp.ezframework.cli.generator;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateProcessorTest {
    @Test
    public void processReplacesPlaceholders() {
        String tmpl = "Hello {{NAME}}, welcome to {{PLACE}}!";
        String out = TemplateProcessor.process(tmpl, Map.of("NAME", "Alice", "PLACE", "Wonderland"));
        assertEquals("Hello Alice, welcome to Wonderland!", out);
    }
}
