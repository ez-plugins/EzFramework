package com.skyblockexp.ezframework.gui.impl;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.bukkit.plugin.java.JavaPlugin;

import static org.junit.jupiter.api.Assertions.*;

public class GuiBootstrapTest {

    public static class TestPlugin extends JavaPlugin {}

    @Test
    public void startAndStopDoNotThrow() throws Exception {
        ServerMock server = MockBukkit.mock();
        TestPlugin plugin = MockBukkit.load(TestPlugin.class);

        GuiBootstrap boot = new GuiBootstrap(plugin);
        boot.start();
        boot.stop();

        MockBukkit.unmock();
    }
}
