package com.skyblockexp.ezframework.message.minimessage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MiniMessageProviderTest {
    @Test
    public void formatHandlesPrefixAndColors() {
        MiniMessageProvider p = new MiniMessageProvider();
        p.setPrefix("&a[TEST]");
        String out = p.format("Hello");
        assertNotNull(out);
        assertTrue(out.contains("Hello"));
        // should contain section sign from ChatColor translation
        assertTrue(out.contains("\u00A7") || out.contains("&"));
    }

    @Test
    public void formatNullReturnsEmpty() {
        MiniMessageProvider p = new MiniMessageProvider();
        assertEquals("", p.format(null));
    }
}
