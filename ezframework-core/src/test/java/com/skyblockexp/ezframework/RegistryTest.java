package com.skyblockexp.ezframework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.bukkit.plugin.java.JavaPlugin;

import static org.junit.jupiter.api.Assertions.*;

public class RegistryTest {
    private JavaPlugin plugin;

    @BeforeEach
    public void setup() {
        plugin = new JavaPlugin();
    }

    @Test
    public void forPluginReturnsSameInstance() {
        Registry r1 = Registry.forPlugin(plugin);
        Registry r2 = Registry.forPlugin(plugin);
        assertSame(r1, r2);
    }

    @Test
    public void registerAndGetByClassAndName() {
        Registry reg = Registry.forPlugin(plugin);
        reg.register("key1", "value");
        assertTrue(reg.get("key1").isPresent());
        assertEquals("value", reg.get("key1", String.class));

        reg.register(Integer.class, 42);
        assertEquals(42, reg.get(Integer.class).intValue());
    }

    @Test
    public void initAllInvokesManagerAndReflection() {
        Registry reg = Registry.forPlugin(plugin);
        TestManager m = new TestManager();
        reg.register(TestManager.class, m);

        ReflectionHolder rh = new ReflectionHolder();
        reg.register("ref", rh);

        reg.initAll();
        assertTrue(m.inited);
        assertTrue(rh.initCalled || rh.loadCalled);

        reg.shutdownAll();
        assertTrue(m.shutdown);
        assertTrue(rh.shutdownCalled || rh.stopCalled || rh.closeCalled);
    }

    static class TestManager extends Manager {
        boolean inited = false;
        boolean shutdown = false;

        @Override
        public void init() {
            inited = true;
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }
    }

    static class ReflectionHolder {
        boolean initCalled = false;
        boolean loadCalled = false;
        boolean shutdownCalled = false;
        boolean stopCalled = false;
        boolean closeCalled = false;

        public void init() { initCalled = true; }
        public void load() { loadCalled = true; }
        public void shutdown() { shutdownCalled = true; }
        public void stop() { stopCalled = true; }
        public void close() { closeCalled = true; }
    }
}
