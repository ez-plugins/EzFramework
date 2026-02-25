package com.skyblockexp.ezframework.gui.impl;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.skyblockexp.ezframework.testutil.TestPlugin;

import static org.junit.jupiter.api.Assertions.*;

public class GuiManagerTest {

    

    @Test
    public void initRegistersService() throws Exception {
        ServerMock server = MockBukkit.mock();
        TestPlugin plugin = MockBukkit.load(TestPlugin.class);

        GuiManager mgr = new GuiManager(plugin);
        mgr.init();

        assertNotNull(mgr.getService());

        MockBukkit.unmock();
    }
}
