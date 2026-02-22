package com.skyblockexp.ezframework.gui.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class EzGUITest {

    @Test
    public void apiSurfaceHasExpectedMethods() throws Exception {
        Class<?> c = EzGUI.class;
        assertNotNull(c.getDeclaredMethod("getProvider"));
        assertNotNull(c.getDeclaredMethod("registerProvider", GuiService.class));

        // ensure INSTANCES map field exists
        Field inst = c.getDeclaredField("INSTANCES");
        assertNotNull(inst);
    }
}
