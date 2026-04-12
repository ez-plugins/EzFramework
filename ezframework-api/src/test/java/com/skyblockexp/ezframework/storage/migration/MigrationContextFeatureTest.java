package com.skyblockexp.ezframework.storage.migration;

import com.skyblockexp.ezframework.storage.StorageProvider;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MigrationContextFeatureTest {

    // -------------------------------------------------------------------------
    // Constructor & simple accessors
    // -------------------------------------------------------------------------

    @Test
    void constructorStoresBothArguments() {
        Object plugin = new Object();
        InMemoryProvider provider = new InMemoryProvider();
        MigrationContext ctx = new MigrationContext(plugin, provider);
        assertSame(plugin, ctx.plugin());
        assertSame(provider, ctx.provider());
    }

    // -------------------------------------------------------------------------
    // migrationCapable()
    // -------------------------------------------------------------------------

    @Test
    void migrationCapableEmptyWhenProviderDoesNotImplementIt() {
        MigrationContext ctx = new MigrationContext(new Object(), new InMemoryProvider());
        assertFalse(ctx.migrationCapable().isPresent());
    }

    @Test
    void migrationCapablePresentWhenProviderImplementsIt() {
        CapableProvider provider = new CapableProvider();
        MigrationContext ctx = new MigrationContext(new Object(), provider);
        assertTrue(ctx.migrationCapable().isPresent());
        assertSame(provider, ctx.migrationCapable().get());
    }

    // -------------------------------------------------------------------------
    // jdbc()
    // -------------------------------------------------------------------------

    @Test
    void jdbcEmptyWhenProviderDoesNotImplementJdbcStorage() {
        MigrationContext ctx = new MigrationContext(new Object(), new InMemoryProvider());
        assertFalse(ctx.jdbc().isPresent());
    }

    @Test
    void jdbcPresentWhenProviderIsJdbcStorage() {
        CapableProvider provider = new CapableProvider();
        MigrationContext ctx = new MigrationContext(new Object(), provider);
        // CapableProvider does NOT implement JdbcStorage in this test fixture
        assertFalse(ctx.jdbc().isPresent());
    }

    // -------------------------------------------------------------------------
    // executeSqlStatements()
    // -------------------------------------------------------------------------

    @Test
    void executeSqlStatementsDelegatesToMigrationCapable() throws Exception {
        CapableProvider provider = new CapableProvider();
        MigrationContext ctx = new MigrationContext(new Object(), provider);
        ctx.executeSqlStatements(List.of("CREATE TABLE t (id VARCHAR(255))"));
        assertEquals(1, provider.executed.size());
        assertEquals("CREATE TABLE t (id VARCHAR(255))", provider.executed.get(0));
    }

    @Test
    void executeSqlStatementsExecutesMultipleInOrder() throws Exception {
        CapableProvider provider = new CapableProvider();
        MigrationContext ctx = new MigrationContext(new Object(), provider);
        ctx.executeSqlStatements(List.of("stmt1", "stmt2", "stmt3"));
        assertEquals(List.of("stmt1", "stmt2", "stmt3"), provider.executed);
    }

    @Test
    void executeSqlStatementsThrowsWhenNotCapable() {
        MigrationContext ctx = new MigrationContext(new Object(), new InMemoryProvider());
        assertThrows(UnsupportedOperationException.class,
                () -> ctx.executeSqlStatements(List.of("SELECT 1")));
    }

    @Test
    void executeSqlStatementsEmptyListDoesNothing() throws Exception {
        CapableProvider provider = new CapableProvider();
        MigrationContext ctx = new MigrationContext(new Object(), provider);
        ctx.executeSqlStatements(Collections.emptyList());
        assertTrue(provider.executed.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    /** A simple non-capable in-memory storage provider. */
    private static class InMemoryProvider implements StorageProvider {
        private final Map<String, Map<String, Object>> store = new HashMap<>();
        @Override public String name() { return "memory"; }
        @Override public void init(Object plugin) {}
        @Override public void close() {}
        @Override public void save(String path, Map<String, Object> data) { store.put(path, data); }
        @Override public Optional<Map<String, Object>> load(String path) { return Optional.ofNullable(store.get(path)); }
        @Override public void delete(String path) { store.remove(path); }
        @Override public boolean exists(String path) { return store.containsKey(path); }
    }

    /** A provider that also implements MigrationCapable. */
    private static class CapableProvider extends InMemoryProvider implements MigrationCapable {
        final List<String> executed = new ArrayList<>();
        @Override public void executeSql(String sql) { executed.add(sql); }
        @Override public String name() { return "capable"; }
    }
}
