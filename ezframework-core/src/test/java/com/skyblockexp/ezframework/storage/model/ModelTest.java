package com.skyblockexp.ezframework.storage.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    static class TestModel extends Model {
        private String foo;

        TestModel(String id) { super(id); }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> m = new HashMap<>();
            m.put("foo", foo);
            m.put("id", getId());
            return m;
        }

        @Override
        public void fromMap(Map<String, Object> map) {
            if (map == null) return;
            Object v = map.get("foo");
            this.foo = v == null ? null : v.toString();
            Object idv = map.get("id");
            if (idv != null) setId(idv.toString());
        }

        String getFoo() { return foo; }
        void setFoo(String f) { foo = f; }
    }

    @Test
    public void getStoragePathWithAndWithoutPrefix() {
        TestModel m = new TestModel("abc");
        assertEquals("abc", m.getStoragePath(null));
        assertEquals("abc", m.getStoragePath(""));
        assertEquals("p/abc", m.getStoragePath("p"));
        assertEquals("prefix/abc", m.getStoragePath("prefix"));
    }

    @Test
    public void toMapAndFromMap_preservesIdAndFields() {
        TestModel m = new TestModel("id1");
        m.setFoo("bar");
        Map<String, Object> map = m.toMap();
        assertEquals("bar", map.get("foo"));
        assertEquals("id1", map.get("id"));

        TestModel n = new TestModel(null);
        n.fromMap(map);
        assertEquals("bar", n.getFoo());
        assertEquals("id1", n.getId());
    }
}
