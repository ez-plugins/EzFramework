package com.skyblockexp.ezframework.gui.impl;

import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

public class BukkitGuiAdaptersTest {

    @Test
    public void wrap_returnsWrappedGuiPlayer() {
        ServerMock server = MockBukkit.mock();
        var player = server.addPlayer();

        var gp = BukkitGuiAdapters.wrap(player);
        assertNotNull(gp);
        assertTrue(gp instanceof BukkitGuiAdapters.BukkitWrappedGuiPlayer);
        var wrapped = (BukkitGuiAdapters.BukkitWrappedGuiPlayer) gp;
        assertEquals(player, wrapped.getPlayer());

        MockBukkit.unmock();
    }
}
