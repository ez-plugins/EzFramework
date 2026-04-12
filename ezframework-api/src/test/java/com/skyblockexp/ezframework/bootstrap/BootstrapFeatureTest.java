package com.skyblockexp.ezframework.bootstrap;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class BootstrapFeatureTest {

    private static final Logger LOGGER = Logger.getLogger("test");

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    @Test
    void constructorRejectsNullLogger() {
        assertThrows(NullPointerException.class, () -> new Bootstrap(null));
    }

    @Test
    void constructorWithValidLoggerSucceeds() {
        Bootstrap b = new Bootstrap(LOGGER);
        assertNotNull(b);
        assertTrue(b.getComponents().isEmpty());
    }

    // -------------------------------------------------------------------------
    // register()
    // -------------------------------------------------------------------------

    @Test
    void registerRejectsNullComponent() {
        Bootstrap b = new Bootstrap(LOGGER);
        assertThrows(NullPointerException.class, () -> b.register(null));
    }

    @Test
    void registerReturnsSameInstanceForChaining() {
        Bootstrap b = new Bootstrap(LOGGER);
        Component c = noopComponent();
        assertSame(b, b.register(c));
    }

    @Test
    void registerAddsComponentToList() {
        Bootstrap b = new Bootstrap(LOGGER);
        Component c1 = noopComponent();
        Component c2 = noopComponent();
        b.register(c1).register(c2);
        assertEquals(2, b.getComponents().size());
        assertSame(c1, b.getComponents().get(0));
        assertSame(c2, b.getComponents().get(1));
    }

    // -------------------------------------------------------------------------
    // getComponents()
    // -------------------------------------------------------------------------

    @Test
    void getComponentsReturnsUnmodifiableList() {
        Bootstrap b = new Bootstrap(LOGGER);
        b.register(noopComponent());
        assertThrows(UnsupportedOperationException.class,
                () -> b.getComponents().add(noopComponent()));
    }

    @Test
    void getComponentsPreservesRegistrationOrder() {
        Bootstrap b = new Bootstrap(LOGGER);
        List<Component> expected = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Component c = noopComponent();
            expected.add(c);
            b.register(c);
        }
        assertEquals(expected, b.getComponents());
    }

    // -------------------------------------------------------------------------
    // startAll()
    // -------------------------------------------------------------------------

    @Test
    void startAllCallsStartOnEachComponent() {
        Bootstrap b = new Bootstrap(LOGGER);
        AtomicInteger started = new AtomicInteger();
        b.register(countingComponent(started)).register(countingComponent(started));
        b.startAll();
        assertEquals(2, started.get());
    }

    @Test
    void startAllCallsComponentsInRegistrationOrder() {
        Bootstrap b = new Bootstrap(LOGGER);
        List<Integer> order = new ArrayList<>();
        b.register(recordingComponent(order, 1)).register(recordingComponent(order, 2)).register(recordingComponent(order, 3));
        b.startAll();
        assertEquals(List.of(1, 2, 3), order);
    }

    @Test
    void startAllContinuesAfterComponentThrows() {
        Bootstrap b = new Bootstrap(LOGGER);
        AtomicInteger started = new AtomicInteger();
        Component throwing = new Component() {
            @Override public void start() throws Exception { throw new RuntimeException("fail"); }
            @Override public void stop() {}
        };
        b.register(throwing).register(countingComponent(started));
        b.startAll();
        assertEquals(1, started.get(), "second component must still start after first throws");
    }

    // -------------------------------------------------------------------------
    // stopAll()
    // -------------------------------------------------------------------------

    @Test
    void stopAllCallsStopOnEachComponent() {
        Bootstrap b = new Bootstrap(LOGGER);
        AtomicInteger stopped = new AtomicInteger();
        b.register(stoppingComponent(stopped)).register(stoppingComponent(stopped));
        b.stopAll();
        assertEquals(2, stopped.get());
    }

    @Test
    void stopAllCallsComponentsInReverseOrder() {
        Bootstrap b = new Bootstrap(LOGGER);
        List<Integer> stopOrder = new ArrayList<>();
        b.register(stoppingRecordingComponent(stopOrder, 1))
                .register(stoppingRecordingComponent(stopOrder, 2))
                .register(stoppingRecordingComponent(stopOrder, 3));
        b.stopAll();
        assertEquals(List.of(3, 2, 1), stopOrder, "stop order must be reverse of registration order");
    }

    @Test
    void stopAllContinuesAfterComponentThrows() {
        Bootstrap b = new Bootstrap(LOGGER);
        AtomicInteger stopped = new AtomicInteger();
        Component throwing = new Component() {
            @Override public void start() {}
            @Override public void stop() throws Exception { throw new RuntimeException("fail"); }
        };
        // register throwing first so reverse order hits normal component second
        b.register(throwing).register(stoppingComponent(stopped));
        b.stopAll();
        assertEquals(1, stopped.get(), "normal component must still stop after throwing component");
    }

    // -------------------------------------------------------------------------
    // reloadAll()
    // -------------------------------------------------------------------------

    @Test
    void reloadAllCallsReloadOnEachComponent() {
        Bootstrap b = new Bootstrap(LOGGER);
        AtomicInteger reloaded = new AtomicInteger();
        Component c1 = new Component() {
            @Override public void start() {}
            @Override public void stop() {}
            @Override public void reload() { reloaded.incrementAndGet(); }
        };
        Component c2 = new Component() {
            @Override public void start() {}
            @Override public void stop() {}
            @Override public void reload() { reloaded.incrementAndGet(); }
        };
        b.register(c1).register(c2);
        b.reloadAll();
        assertEquals(2, reloaded.get());
    }

    @Test
    void reloadAllDefaultNoopDoesNotThrow() {
        Bootstrap b = new Bootstrap(LOGGER);
        b.register(noopComponent());
        assertDoesNotThrow(() -> b.reloadAll());
    }

    @Test
    void reloadAllContinuesAfterComponentThrows() {
        Bootstrap b = new Bootstrap(LOGGER);
        AtomicBoolean secondReloaded = new AtomicBoolean(false);
        Component throwing = new Component() {
            @Override public void start() {}
            @Override public void stop() {}
            @Override public void reload() throws Exception { throw new RuntimeException("reload fail"); }
        };
        Component ok = new Component() {
            @Override public void start() {}
            @Override public void stop() {}
            @Override public void reload() { secondReloaded.set(true); }
        };
        b.register(throwing).register(ok);
        b.reloadAll();
        assertTrue(secondReloaded.get());
    }

    // -------------------------------------------------------------------------
    // Component default reload()
    // -------------------------------------------------------------------------

    @Test
    void componentDefaultReloadIsNoopAndDoesNotThrow() throws Exception {
        Component c = noopComponent();
        assertDoesNotThrow(() -> c.reload());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Component noopComponent() {
        return new Component() {
            @Override public void start() {}
            @Override public void stop() {}
        };
    }

    private static Component countingComponent(AtomicInteger counter) {
        return new Component() {
            @Override public void start() { counter.incrementAndGet(); }
            @Override public void stop() {}
        };
    }

    private static Component stoppingComponent(AtomicInteger counter) {
        return new Component() {
            @Override public void start() {}
            @Override public void stop() { counter.incrementAndGet(); }
        };
    }

    private static Component recordingComponent(List<Integer> order, int id) {
        return new Component() {
            @Override public void start() { order.add(id); }
            @Override public void stop() {}
        };
    }

    private static Component stoppingRecordingComponent(List<Integer> order, int id) {
        return new Component() {
            @Override public void start() {}
            @Override public void stop() { order.add(id); }
        };
    }
}
