package com.skyblockexp.ezframework.gui.impl;

import com.skyblockexp.ezframework.gui.api.GuiItem;
import com.skyblockexp.ezframework.gui.api.MenuBuilder;
import com.skyblockexp.ezframework.gui.api.MenuDefinition;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import com.skyblockexp.ezframework.testutil.TestPlugin;

import static org.junit.jupiter.api.Assertions.*;

public class BukkitGuiServiceTest {

    

    @Test
    public void openMenuOpensInventory() {
        ServerMock server = MockBukkit.mock();
        TestPlugin plugin = MockBukkit.load(TestPlugin.class);

        BukkitGuiService service = new BukkitGuiService();
        service.init(plugin);

        var player = server.addPlayer();

        MenuDefinition menu = MenuBuilder.create()
                .title("T")
                .size(9)
                .item(0, new GuiItem("STONE", 1, "Stone", null))
                .build();

        service.openMenu(com.skyblockexp.ezframework.gui.impl.BukkitGuiAdapters.wrap(player), menu);

        assertNotNull(player.getOpenInventory());
        assertEquals(9, player.getOpenInventory().getTopInventory().getSize());

        MockBukkit.unmock();
    }
}
