package com.skyblockexp.ezframework.storage.model;

import com.skyblockexp.ezframework.storage.StorageProvider;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class EzModelTest {

    static class TestEzModel extends EzModel {
        TestEzModel(String id) { super(id); }
        TestEzModel(String id, Map<String, Object> attrs) { super(id, attrs); }
    }

    static class InMemoryProvider implements StorageProvider {
        private final Map<String, Map<String, Object>> store = new HashMap<>();

        @Override
        public String name() { return "in-memory"; }

        @Override
        public void init(Object plugin) { }

        @Override
        public void close() { }

        @Override
        public void save(String path, Map<String, Object> data) {
            store.put(path, new HashMap<>(data));
        }

        @Override
        public Optional<Map<String, Object>> load(String path) {
            Map<String, Object> v = store.get(path);
            return v == null ? Optional.empty() : Optional.of(new HashMap<>(v));
        }

        @Override
        public void delete(String path) { store.remove(path); }

        @Override
        public boolean exists(String path) { return store.containsKey(path); }
    }

    @Test
    public void setAndGetIdViaAttributes() {
        TestEzModel m = new TestEzModel("x1");
        assertEquals("x1", m.getId());
        m.set("id", "x2");
        assertEquals("x2", m.getId());
        assertEquals("x2", m.get("id"));
    }

    @Test
    public void getAsConversionsAndDefaults() {
        TestEzModel m = new TestEzModel("t");
        m.set("n", 42);
        assertEquals(Integer.valueOf(42), m.getAs("n", Integer.class));
        m.set("l", 1234567890123L);
        assertEquals(Long.valueOf(1234567890123L), m.getAs("l", Long.class));
        m.set("s", 5);
        assertEquals("5", m.getAs("s", String.class));
        assertEquals(Integer.valueOf(7), m.getAs("missing", Integer.class, 7));
    }

    @Test
    public void toMapAndFromMap_includeId() {
        TestEzModel m = new TestEzModel("u1");
        m.set("a", "one");
        Map<String, Object> map = m.toMap();
        assertEquals("one", map.get("a"));

        TestEzModel n = new TestEzModel(null);
        Map<String, Object> in = new HashMap<>();
        in.put("id", "u1");
        in.put("a", "one");
        n.fromMap(in);
        assertEquals("u1", n.getId());
        assertEquals("one", n.get("a"));
    }

    @Test
    public void fillableAndGuardedControlMassAssignment() {
        TestEzModel m = new TestEzModel("f1");
        Map<String, Object> src = new HashMap<>();
        src.put("a", 1);
        src.put("b", 2);
        // by default no fillable means guarded controls; none guarded -> all assignable
        m.fill(src);
        assertEquals(1, m.getAs("a", Integer.class));
        assertEquals(2, m.getAs("b", Integer.class));

        // guard a and refill -> a should be ignored
        m = new TestEzModel("f2");
        m.setGuarded("a");
        m.fill(src);
        assertNull(m.getAs("a", Integer.class));
        assertEquals(2, m.getAs("b", Integer.class));

        // set fillable to only a -> only a assigned
        m = new TestEzModel("f3");
        m.setFillable("a");
        m.fill(src);
        assertEquals(1, m.getAs("a", Integer.class));
        assertNull(m.getAs("b", Integer.class));
    }

    @Test
    public void attributesAreUnmodifiableView() {
        TestEzModel m = new TestEzModel("v1");
        m.set("k", "v");
        Map<String, Object> attrs = m.attributes();
        assertThrows(UnsupportedOperationException.class, () -> attrs.put("x", "y"));
    }

    @Test
    public void repositorySaveFindDeleteExists_flow() throws Exception {
        InMemoryProvider prov = new InMemoryProvider();
        ModelFactory<TestEzModel> factory = (id, data) -> new TestEzModel(id);
        ModelRepository<TestEzModel> repo = new ModelRepository<>(prov, "pref", factory);

        TestEzModel m = new TestEzModel("id42");
        m.set("score", 9001);
        // save via repo
        repo.save(m);
        assertTrue(repo.exists("id42"));

        Optional<TestEzModel> found = repo.find("id42");
        assertTrue(found.isPresent());
        TestEzModel f = found.get();
        assertEquals("id42", f.getId());
        assertEquals(9001, f.getAs("score", Integer.class));

        repo.delete("id42");
        assertFalse(repo.exists("id42"));
    }
}
