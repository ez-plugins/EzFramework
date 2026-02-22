package com.skyblockexp.ezframework.gui;

import com.skyblockexp.ezframework.gui.impl.BukkitGuiService;
import com.skyblockexp.ezframework.gui.api.EzGUI;
import com.skyblockexp.ezframework.gui.api.GuiItem;
import com.skyblockexp.ezframework.gui.api.MenuBuilder;
import com.skyblockexp.ezframework.gui.api.MenuDefinition;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BukkitGuiServiceIntegrationTest {

    public static class TestPlugin extends JavaPlugin {}

    @AfterEach
    public void teardown() {
        try { MockBukkit.unmock(); } catch (Exception ignored) {}
    }

    @Test
    public void openMenuAndClickDispatches() {
        ServerMock server = MockBukkit.mock();
        TestPlugin plugin = MockBukkit.load(TestPlugin.class);

        BukkitGuiService service = new BukkitGuiService();
        service.init(plugin);

        var player = server.addPlayer();

        AtomicBoolean clicked = new AtomicBoolean(false);

        MenuDefinition menu = MenuBuilder.create()
                .title("Test Menu")
                .size(9)
                .item(0, new GuiItem("STONE", 1, "Stone", null))
                .onClick(ctx -> clicked.set(true))
                .build();

        service.openMenu(com.skyblockexp.ezframework.gui.impl.BukkitGuiAdapters.wrap(player), menu);

        // simulate a click on slot 0
        InventoryView view = player.getOpenInventory();
        InventoryClickEvent clickEvent = new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.PICKUP_ALL, 0);
        server.getPluginManager().callEvent(clickEvent);

        assertTrue(clicked.get());
    }
}
