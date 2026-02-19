package com.skyblockexp.ezframework.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubcommandTest {
    @Test
    public void matchesNameAndAlias() {
        Subcommand s = new Subcommand("foo", "perm", false, "bar") {
            @Override
            public boolean execute(org.bukkit.command.CommandSender sender, String[] args) {
                return true;
            }
        };

        assertTrue(s.matches("foo"));
        assertTrue(s.matches("bar"));
        assertFalse(s.matches("baz"));
        assertEquals("perm", s.getPermission());
        assertFalse(s.isDefault());
    }
}
