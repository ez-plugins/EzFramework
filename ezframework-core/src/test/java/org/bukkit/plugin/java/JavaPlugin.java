package org.bukkit.plugin.java;

import java.util.logging.Logger;

/**
 * Minimal test double for org.bukkit.plugin.java.JavaPlugin used in unit tests.
 */
public class JavaPlugin {
    private final Logger logger = Logger.getLogger("TestPlugin");

    public Logger getLogger() {
        return logger;
    }
}
