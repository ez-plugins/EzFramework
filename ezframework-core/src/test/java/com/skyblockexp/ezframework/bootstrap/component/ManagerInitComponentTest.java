package com.skyblockexp.ezframework.bootstrap.component;

import com.skyblockexp.ezframework.Registry;
import com.skyblockexp.ezframework.bootstrap.component.ManagerInitComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagerInitComponentTest {
    @Test
    public void startInvokesRegistryInitAll() throws Exception {
        org.bukkit.plugin.java.JavaPlugin plugin = new org.bukkit.plugin.java.JavaPlugin();
        Registry reg = Registry.forPlugin(plugin);
        TestManager m = new TestManager();
        reg.register(TestManager.class, m);

        ManagerInitComponent c = new ManagerInitComponent(plugin);
        c.start();
        assertTrue(m.initCalled);
        c.stop();
        assertTrue(m.shutdownCalled);
    }

    static class TestManager extends com.skyblockexp.ezframework.Manager {
        boolean initCalled = false;
        boolean shutdownCalled = false;

        @Override
        public void init() { initCalled = true; }

        @Override
        public void shutdown() { shutdownCalled = true; }
    }
}
