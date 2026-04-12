package com.skyblockexp.ezframework.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class StorageRegistryFeatureTest {

    /** Providers registered by a test — their names are tracked for cleanup. */
    private final java.util.Set<String> registeredNames = new java.util.LinkedHashSet<>();

    @AfterEach
    void teardown() {
        // StorageRegistry is static — remove test providers to avoid leaking state
        // between tests.  We achieve this by replacing the entry with a do-nothing
        // "sentinel" that immediately closes itself; the internal map has no remove(),
        // so we close all and re-register nothing.
        StorageRegistry.closeAll();
        // Re-register nothing — the static map is now "empty" for purposes of our
        // tests, since all providers have been closed and get() still returns them.
        // Workaround: use unique names per test so there is no cross-contamination.
    }

    // -------------------------------------------------------------------------
    // register() / get()
    // -------------------------------------------------------------------------

    @Test
    void registerAndGetByName() {
        InMemoryProvider p = new InMemoryProvider("reg-test-1");
        StorageRegistry.register(p);
        assertSame(p, StorageRegistry.get("reg-test-1"));
    }

    @Test
    void registerRejectsNull() {
        assertThrows(NullPointerException.class, () -> StorageRegistry.register(null));
    }

    @Test
    void getReturnsNullForUnknownName() {
        assertNull(StorageRegistry.get("completely-unknown-xyz"));
    }

    // -------------------------------------------------------------------------
    // getAll()
    // -------------------------------------------------------------------------

    @Test
    void getAllContainsRegisteredProvider() {
        InMemoryProvider p = new InMemoryProvider("reg-test-2");
        StorageRegistry.register(p);
        assertTrue(StorageRegistry.getAll().containsKey("reg-test-2"));
        assertSame(p, StorageRegistry.getAll().get("reg-test-2"));
    }

    @Test
    void getAllIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> StorageRegistry.getAll().put("bad", new InMemoryProvider("bad")));
    }

    // -------------------------------------------------------------------------
    // initAll()
    // -------------------------------------------------------------------------

    @Test
    void initAllCallsInitOnRegisteredProvider() {
        TrackingProvider p = new TrackingProvider("reg-test-3");
        StorageRegistry.register(p);
        StorageRegistry.initAll("plugin-context");
        assertTrue(p.initialized, "provider.init() should have been called");
        assertEquals("plugin-context", p.initPlugin);
    }

    @Test
    void initAllDoesNotThrowWhenProviderThrows() {
        FailingProvider p = new FailingProvider("reg-test-fail");
        StorageRegistry.register(p);
        assertDoesNotThrow(() -> StorageRegistry.initAll("plugin"));
    }

    // -------------------------------------------------------------------------
    // closeAll()
    // -------------------------------------------------------------------------

    @Test
    void closeAllCallsCloseOnRegisteredProvider() {
        TrackingProvider p = new TrackingProvider("reg-test-4");
        StorageRegistry.register(p);
        StorageRegistry.closeAll();
        assertTrue(p.closed);
    }

    @Test
    void closeAllDoesNotThrowWhenProviderThrows() {
        FailingProvider p = new FailingProvider("reg-test-close-fail");
        StorageRegistry.register(p);
        assertDoesNotThrow(() -> StorageRegistry.closeAll());
    }

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    static class InMemoryProvider implements StorageProvider {
        private final String name;
        private final Map<String, Map<String, Object>> store = new HashMap<>();

        InMemoryProvider(String name) { this.name = name; }

        @Override public String name() { return name; }
        @Override public void init(Object plugin) {}
        @Override public void close() {}
        @Override public void save(String path, Map<String, Object> data) { store.put(path, data); }
        @Override public Optional<Map<String, Object>> load(String path) { return Optional.ofNullable(store.get(path)); }
        @Override public void delete(String path) { store.remove(path); }
        @Override public boolean exists(String path) { return store.containsKey(path); }
    }

    static class TrackingProvider extends InMemoryProvider {
        boolean initialized = false;
        boolean closed = false;
        Object initPlugin = null;

        TrackingProvider(String name) { super(name); }

        @Override public void init(Object plugin) { this.initialized = true; this.initPlugin = plugin; }
        @Override public void close() { this.closed = true; }
    }

    static class FailingProvider extends InMemoryProvider {
        FailingProvider(String name) { super(name); }
        @Override public void init(Object plugin) { throw new RuntimeException("init fail"); }
        @Override public void close() { throw new RuntimeException("close fail"); }
    }
}
