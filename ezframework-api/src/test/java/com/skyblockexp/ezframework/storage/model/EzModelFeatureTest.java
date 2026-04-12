package com.skyblockexp.ezframework.storage.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EzModelFeatureTest {

    // -------------------------------------------------------------------------
    // set() / get()
    // -------------------------------------------------------------------------

    @Test
    void setAndGetAttribute() {
        TestModel m = new TestModel("1");
        m.set("name", "Alice");
        assertEquals("Alice", m.get("name"));
    }

    @Test
    void setWithIdKeyUpdatesModelId() {
        TestModel m = new TestModel("old-id");
        m.set("id", "new-id");
        assertEquals("new-id", m.getId());
    }

    @Test
    void getReturnsNullForMissingKey() {
        TestModel m = new TestModel("1");
        assertNull(m.get("missing"));
    }

    @Test
    void getWithIdKeyReturnsModelId() {
        TestModel m = new TestModel("my-id");
        assertEquals("my-id", m.get("id"));
    }

    // -------------------------------------------------------------------------
    // getAs() without default
    // -------------------------------------------------------------------------

    @Test
    void getAsReturnsValueWhenTypeMatches() {
        TestModel m = new TestModel("1");
        m.set("score", 100);
        assertEquals(100, m.getAs("score", Integer.class));
    }

    @Test
    void getAsConvertsLongToInteger() {
        TestModel m = new TestModel("1");
        m.set("count", 5L);
        assertEquals(5, m.getAs("count", Integer.class));
    }

    @Test
    void getAsConvertsIntToLong() {
        TestModel m = new TestModel("1");
        m.set("big", 1000000000);
        assertEquals(1000000000L, m.getAs("big", Long.class));
    }

    @Test
    void getAsConvertsAnyValueToString() {
        TestModel m = new TestModel("1");
        m.set("num", 42);
        assertEquals("42", m.getAs("num", String.class));
    }

    @Test
    void getAsReturnsNullForMissingKey() {
        TestModel m = new TestModel("1");
        assertNull(m.getAs("missing", String.class));
    }

    @Test
    void getAsReturnsNullForIncompatibleType() {
        TestModel m = new TestModel("1");
        m.set("obj", "text");
        // String cannot be cast to Integer through the simple conversions
        assertNull(m.getAs("obj", Integer.class));
    }

    // -------------------------------------------------------------------------
    // getAs() with default
    // -------------------------------------------------------------------------

    @Test
    void getAsWithDefaultReturnsPresentValue() {
        TestModel m = new TestModel("1");
        m.set("coins", 50);
        assertEquals(50, m.getAs("coins", Integer.class, 0));
    }

    @Test
    void getAsWithDefaultReturnsFallbackForMissingKey() {
        TestModel m = new TestModel("1");
        assertEquals(99, m.getAs("missing", Integer.class, 99));
    }

    @Test
    void getAsWithDefaultReturnsFallbackForNullValue() {
        TestModel m = new TestModel("1");
        m.set("x", null);
        assertEquals("default", m.getAs("x", String.class, "default"));
    }

    // -------------------------------------------------------------------------
    // setFillable() / setGuarded()
    // -------------------------------------------------------------------------

    @Test
    void fillableRestrictsDirectSetViaFill() {
        TestModel m = new TestModel("1");
        m.setFillable("name");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("name", "Alice");
        attrs.put("secret", "hidden");
        m.fill(attrs);
        assertEquals("Alice", m.get("name"));
        assertNull(m.get("secret"), "non-fillable key should be rejected");
    }

    @Test
    void guardedPreventsAssignment() {
        TestModel m = new TestModel("1");
        m.setGuarded("secret");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("name", "Alice");
        attrs.put("secret", "hidden");
        m.fill(attrs);
        assertEquals("Alice", m.get("name"));
        assertNull(m.get("secret"), "guarded key should be rejected by fill()");
    }

    @Test
    void idIsAlwaysGuardedByFill() {
        TestModel m = new TestModel("original");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("id", "injected");
        m.fill(attrs);
        assertEquals("original", m.getId(), "fill() must not overwrite id");
    }

    @Test
    void setFillableReturnsSameModelForChaining() {
        TestModel m = new TestModel("1");
        assertSame(m, m.setFillable("name"));
    }

    @Test
    void setGuardedReturnsSameModelForChaining() {
        TestModel m = new TestModel("1");
        assertSame(m, m.setGuarded("secret"));
    }

    // -------------------------------------------------------------------------
    // fill()
    // -------------------------------------------------------------------------

    @Test
    void fillWithNoFillableOrGuardedAssignsAll() {
        TestModel m = new TestModel("1");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("a", 1);
        attrs.put("b", 2);
        m.fill(attrs);
        assertEquals(1, m.get("a"));
        assertEquals(2, m.get("b"));
    }

    @Test
    void fillNullMapDoesNothing() {
        TestModel m = new TestModel("1");
        m.set("x", "original");
        m.fill(null);
        assertEquals("original", m.get("x"));
    }

    @Test
    void fillReturnsSameModelForChaining() {
        TestModel m = new TestModel("1");
        assertSame(m, m.fill(new HashMap<>()));
    }

    // -------------------------------------------------------------------------
    // attributes()
    // -------------------------------------------------------------------------

    @Test
    void attributesIsUnmodifiable() {
        TestModel m = new TestModel("1");
        m.set("x", 1);
        assertThrows(UnsupportedOperationException.class, () -> m.attributes().put("y", 2));
    }

    @Test
    void attributesReflectsCurrentState() {
        TestModel m = new TestModel("1");
        m.set("name", "Bob");
        assertTrue(m.attributes().containsKey("name"));
        assertEquals("Bob", m.attributes().get("name"));
    }

    // -------------------------------------------------------------------------
    // toMap() / fromMap()
    // -------------------------------------------------------------------------

    @Test
    void toMapReturnsAttributesCopy() {
        TestModel m = new TestModel("1");
        m.set("k", "v");
        Map<String, Object> map = m.toMap();
        assertEquals("v", map.get("k"));
    }

    @Test
    void toMapIsMutable() {
        TestModel m = new TestModel("1");
        m.set("k", "v");
        assertDoesNotThrow(() -> m.toMap().put("extra", "x"));
    }

    @Test
    void fromMapSetsAttributesFromMap() {
        TestModel m = new TestModel("1");
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Charlie");
        map.put("level", 10);
        m.fromMap(map);
        assertEquals("Charlie", m.get("name"));
        assertEquals(10, m.get("level"));
    }

    @Test
    void fromMapUpdatesIdWhenPresentInMap() {
        TestModel m = new TestModel("old");
        Map<String, Object> map = new HashMap<>();
        map.put("id", "new");
        m.fromMap(map);
        assertEquals("new", m.getId());
    }

    @Test
    void fromMapNullClearsAttributes() {
        TestModel m = new TestModel("1");
        m.set("k", "v");
        m.fromMap(null);
        assertNull(m.get("k"));
    }

    // -------------------------------------------------------------------------
    // queryBuilder()
    // -------------------------------------------------------------------------

    @Test
    void queryBuilderReturnsNonNull() {
        assertNotNull(EzModel.queryBuilder());
    }

    // -------------------------------------------------------------------------
    // Test fixture
    // -------------------------------------------------------------------------

    static class TestModel extends EzModel {
        TestModel(String id) { super(id); }
        TestModel(String id, Map<String, Object> attrs) { super(id, attrs); }
    }
}
