package com.skyblockexp.ezframework;

import com.skyblockexp.ezframework.bootstrap.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EzPluginTest {
    @Test
    public void onEnableRegistersAndStartsComponents() {
        TestEzPlugin p = new TestEzPlugin();
        assertFalse(p.started);
        p.onEnable();
        assertTrue(p.started);
        // bootstrap should have our component
        assertEquals(1, p.getBootstrap().getComponents().size());
        p.onDisable();
        assertTrue(p.stopped);
    }

    static class TestEzPlugin extends EzPlugin {
        boolean started = false;
        boolean stopped = false;

        @Override
        protected java.util.List<Component> components() {
            return List.of(new Component() {
                @Override
                public void start() { started = true; }

                @Override
                public void stop() { stopped = true; }
            });
        }
    }
}
