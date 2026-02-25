package com.skyblockexp.ezframework.gui.impl;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.skyblockexp.ezframework.testutil.TestPlugin;

import static org.junit.jupiter.api.Assertions.*;

public class GuiBootstrapTest {

    

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
