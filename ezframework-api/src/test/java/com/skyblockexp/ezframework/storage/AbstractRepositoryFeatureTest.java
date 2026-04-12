package com.skyblockexp.ezframework.storage;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractRepositoryFeatureTest {

    // -------------------------------------------------------------------------
    // find()
    // -------------------------------------------------------------------------

    @Test
    void findReturnsEmptyWhenNotPresent() throws Exception {
        TestRepo repo = new TestRepo(new InMemoryProvider());
        Optional<Item> result = repo.find("nonexistent");
        assertFalse(result.isPresent());
    }

    @Test
    void findReturnsPresentAfterSave() throws Exception {
        InMemoryProvider provider = new InMemoryProvider();
        TestRepo repo = new TestRepo(provider);
        Item item = new Item("user/42", "Alice");
        repo.save(item);
        Optional<Item> found = repo.find("user/42");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().name);
    }

    // -------------------------------------------------------------------------
    // findAll()
    // -------------------------------------------------------------------------

    @Test
    void findAllDefaultReturnsEmptyList() throws Exception {
        TestRepo repo = new TestRepo(new InMemoryProvider());
        List<Item> all = repo.findAll();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    // -------------------------------------------------------------------------
    // save()
    // -------------------------------------------------------------------------

    @Test
    void saveStoresEntityInProvider() throws Exception {
        InMemoryProvider provider = new InMemoryProvider();
        TestRepo repo = new TestRepo(provider);
        Item item = new Item("user/1", "Bob");
        repo.save(item);
        assertTrue(provider.exists("user/1"), "provider should have stored the entity");
    }

    @Test
    void saveOverwritesExistingEntity() throws Exception {
        InMemoryProvider provider = new InMemoryProvider();
        TestRepo repo = new TestRepo(provider);
        repo.save(new Item("user/1", "Alice"));
        repo.save(new Item("user/1", "Alicia"));
        Optional<Item> found = repo.find("user/1");
        assertTrue(found.isPresent());
        assertEquals("Alicia", found.get().name);
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    void deleteRemovesEntityFromProvider() throws Exception {
        InMemoryProvider provider = new InMemoryProvider();
        TestRepo repo = new TestRepo(provider);
        Item item = new Item("user/99", "Delete Me");
        repo.save(item);
        repo.delete("user/99");
        assertFalse(provider.exists("user/99"), "provider should not have the entity after delete");
    }

    @Test
    void deleteNonExistentDoesNotThrow() throws Exception {
        TestRepo repo = new TestRepo(new InMemoryProvider());
        assertDoesNotThrow(() -> repo.delete("user/missing"));
    }

    // -------------------------------------------------------------------------
    // pathFor() (via prefix)
    // -------------------------------------------------------------------------

    @Test
    void prefixIsPrependedToId() throws Exception {
        InMemoryProvider provider = new InMemoryProvider();
        TestRepo repo = new TestRepo(provider, "items/");
        Item item = new Item("42", "Prefixed");
        repo.save(item);
        assertTrue(provider.exists("items/42"), "path should include prefix");
    }

    @Test
    void nullPrefixTreatedAsEmpty() throws Exception {
        InMemoryProvider provider = new InMemoryProvider();
        TestRepo repo = new TestRepo(provider, null);
        Item item = new Item("abc", "NullPrefix");
        repo.save(item);
        assertTrue(provider.exists("abc"));
    }

    // -------------------------------------------------------------------------
    // provider()
    // -------------------------------------------------------------------------

    @Test
    void providerAccessorReturnsConstructorArgument() {
        InMemoryProvider provider = new InMemoryProvider();
        TestRepo repo = new TestRepo(provider);
        assertSame(provider, repo.exposedProvider());
    }

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    /** Simple entity. */
    record Item(String id, String name) {}

    /** Concrete repo for Item. */
    static class TestRepo extends AbstractRepository<Item, String> {
        TestRepo(StorageProvider provider) { super(provider, ""); }
        TestRepo(StorageProvider provider, String prefix) { super(provider, prefix); }

        @Override protected Map<String, Object> toMap(Item item) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", item.name());
            return m;
        }

        @Override protected Item fromMap(Map<String, Object> map) {
            return new Item((String) map.get("id"), (String) map.get("name"));
        }

        @Override protected String extractId(Item item) { return item.id(); }

        // Expose protected provider() for assertions
        StorageProvider exposedProvider() { return provider(); }
    }

    /** Simple in-memory StorageProvider. */
    static class InMemoryProvider implements StorageProvider {
        private final Map<String, Map<String, Object>> store = new HashMap<>();

        @Override public String name() { return "memory"; }
        @Override public void init(Object plugin) {}
        @Override public void close() {}

        @Override public void save(String path, Map<String, Object> data) {
            Map<String, Object> copy = new HashMap<>(data);
            copy.put("id", path); // store id for reconstruction
            store.put(path, copy);
        }

        @Override public Optional<Map<String, Object>> load(String path) {
            return Optional.ofNullable(store.get(path));
        }

        @Override public void delete(String path) { store.remove(path); }
        @Override public boolean exists(String path) { return store.containsKey(path); }
    }
}
