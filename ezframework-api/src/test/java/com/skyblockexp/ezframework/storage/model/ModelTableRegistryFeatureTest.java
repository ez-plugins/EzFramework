package com.skyblockexp.ezframework.storage.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTableRegistryFeatureTest {

    // Use a unique prefix per test to avoid static-state contamination.

    @Test
    void registerAndGetReturnTableMeta() {
        Map<String, String> cols = new HashMap<>();
        cols.put("name", "VARCHAR(255)");
        ModelTableRegistry.register("mtr-test-1", "users", cols);

        ModelTableRegistry.TableMeta meta = ModelTableRegistry.get("mtr-test-1");
        assertNotNull(meta);
        assertEquals("users", meta.tableName());
    }

    @Test
    void getReturnsNullForUnknownPrefix() {
        assertNull(ModelTableRegistry.get("mtr-unknown-prefix"));
    }

    @Test
    void registeredMetaAppearsInAll() {
        Map<String, String> cols = new HashMap<>();
        cols.put("level", "INT");
        ModelTableRegistry.register("mtr-test-2", "players", cols);

        assertTrue(ModelTableRegistry.all().containsKey("mtr-test-2"));
    }

    @Test
    void allReturnsUnmodifiableMap() {
        assertThrows(UnsupportedOperationException.class,
                () -> ModelTableRegistry.all().put("mtr-x", null));
    }

    @Test
    void tableMetaColumnsReturnsCorrectValues() {
        Map<String, String> cols = new HashMap<>();
        cols.put("email", "VARCHAR(100)");
        cols.put("age", "INT");
        ModelTableRegistry.register("mtr-test-3", "accounts", cols);

        Map<String, String> returned = ModelTableRegistry.get("mtr-test-3").columns();
        assertEquals("VARCHAR(100)", returned.get("email"));
        assertEquals("INT", returned.get("age"));
    }

    @Test
    void tableMetaColumnsIsUnmodifiable() {
        Map<String, String> cols = new HashMap<>();
        cols.put("x", "TEXT");
        ModelTableRegistry.register("mtr-test-4", "things", cols);

        assertThrows(UnsupportedOperationException.class,
                () -> ModelTableRegistry.get("mtr-test-4").columns().put("y", "INT"));
    }

    @Test
    void mutatingOriginalMapDoesNotAffectRegisteredMeta() {
        Map<String, String> cols = new HashMap<>();
        cols.put("a", "TEXT");
        ModelTableRegistry.register("mtr-test-5", "stuff", cols);

        cols.put("b", "INT"); // mutate original after registration

        assertFalse(ModelTableRegistry.get("mtr-test-5").columns().containsKey("b"),
                "registered columns should be a snapshot, not the original map");
    }

    @Test
    void registerOverwritesPreviousMeta() {
        Map<String, String> cols1 = new HashMap<>();
        cols1.put("old", "TEXT");
        ModelTableRegistry.register("mtr-test-6", "tableA", cols1);

        Map<String, String> cols2 = new HashMap<>();
        cols2.put("new", "INT");
        ModelTableRegistry.register("mtr-test-6", "tableB", cols2);

        ModelTableRegistry.TableMeta meta = ModelTableRegistry.get("mtr-test-6");
        assertEquals("tableB", meta.tableName());
        assertTrue(meta.columns().containsKey("new"));
        assertFalse(meta.columns().containsKey("old"));
    }

    @Test
    void tableMetaTableNameMatchesRegistered() {
        Map<String, String> cols = new HashMap<>();
        ModelTableRegistry.register("mtr-test-7", "exact_table_name", cols);
        assertEquals("exact_table_name", ModelTableRegistry.get("mtr-test-7").tableName());
    }
}
