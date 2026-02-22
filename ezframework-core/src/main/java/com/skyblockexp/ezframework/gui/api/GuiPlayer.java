package com.skyblockexp.ezframework.gui.api;

import java.util.UUID;

/**
 * Minimal, server-agnostic player handle used in GUI callbacks.
 * Implementations should adapt their native player objects to this
 * interface so core code can remain platform-independent.
 */
public interface GuiPlayer {
    UUID getUniqueId();

    String getName();

    void sendMessage(String message);
}
