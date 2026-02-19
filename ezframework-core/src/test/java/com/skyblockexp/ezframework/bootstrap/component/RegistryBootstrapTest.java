package com.skyblockexp.ezframework.bootstrap.component;

import com.skyblockexp.ezframework.Registry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistryBootstrapTest {
    @Test
    public void startCreatesRegistryInstance() throws Exception {
        org.bukkit.plugin.java.JavaPlugin plugin = new org.bukkit.plugin.java.JavaPlugin();
        RegistryBootstrap rb = new RegistryBootstrap(plugin);
        rb.start();
        Registry r = Registry.forPlugin(plugin);
        assertNotNull(r);
    }
}
