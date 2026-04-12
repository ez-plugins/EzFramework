package com.skyblockexp.ezframework.proxy.event;

import com.skyblockexp.ezframework.proxy.EzContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class ProxyEventManagerFeatureTest {

    private static final Logger SILENT_LOGGER = Logger.getLogger("test-silent");

    private ProxyEventManager manager;

    @BeforeEach
    void setUp() {
        manager = new ProxyEventManager(SILENT_LOGGER);
    }

    // -------------------------------------------------------------------------
    // Basic dispatch
    // -------------------------------------------------------------------------

    @Test
    void handlerFiresOnMatchingEventType() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void onEvent(TestEvent event) {
                fired.add("hit");
            }
        });

        manager.fireEvent(new TestEvent("player", "lobby"));

        assertEquals(List.of("hit"), fired);
    }

    @Test
    void fireEventWithNoListenersIsNoOp() {
        // must not throw
        assertDoesNotThrow(() -> manager.fireEvent(new TestEvent("player", "lobby")));
    }

    @Test
    void handlerDoesNotFireForDifferentEventType() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void onEvent(TestEvent event) {
                fired.add("hit");
            }
        });

        manager.fireEvent(new OtherTestEvent());

        assertTrue(fired.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Priority ordering
    // -------------------------------------------------------------------------

    @Test
    void handlersFireInPriorityOrder() {
        List<String> order = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener(priority = EventPriority.HIGHEST)
            public void onHighest(TestEvent e) { order.add("HIGHEST"); }

            @PacketListener(priority = EventPriority.LOWEST)
            public void onLowest(TestEvent e) { order.add("LOWEST"); }

            @PacketListener(priority = EventPriority.NORMAL)
            public void onNormal(TestEvent e) { order.add("NORMAL"); }

            @PacketListener(priority = EventPriority.HIGH)
            public void onHigh(TestEvent e) { order.add("HIGH"); }

            @PacketListener(priority = EventPriority.LOW)
            public void onLow(TestEvent e) { order.add("LOW"); }

            @PacketListener(priority = EventPriority.MONITOR)
            public void onMonitor(TestEvent e) { order.add("MONITOR"); }
        });

        manager.fireEvent(new TestEvent("p", "s"));

        assertEquals(
                List.of("LOWEST", "LOW", "NORMAL", "HIGH", "HIGHEST", "MONITOR"),
                order,
                "Handlers must fire in EventPriority ordinal order");
    }

    // -------------------------------------------------------------------------
    // Cancellation + ignoreCancelled
    // -------------------------------------------------------------------------

    @Test
    void ignoreCancelledSkipsHandlerWhenEventCancelled() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener(priority = EventPriority.LOW)
            public void cancel(TestEvent e) { e.setCancelled(true); }

            @PacketListener(priority = EventPriority.NORMAL, ignoreCancelled = true)
            public void skipped(TestEvent e) { fired.add("should-not-fire"); }
        });

        manager.fireEvent(new TestEvent("p", "s"));

        assertTrue(fired.isEmpty(), "ignoreCancelled handler must be skipped when event is cancelled");
    }

    @Test
    void ignoreCancelledFalseStillFiresWhenEventCancelled() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener(priority = EventPriority.LOW)
            public void cancel(TestEvent e) { e.setCancelled(true); }

            @PacketListener(priority = EventPriority.NORMAL, ignoreCancelled = false)
            public void alwaysFires(TestEvent e) { fired.add("hit"); }
        });

        manager.fireEvent(new TestEvent("p", "s"));

        assertEquals(List.of("hit"), fired);
    }

    @Test
    void monitorAlwaysFiresEvenWhenCancelled() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener(priority = EventPriority.LOWEST)
            public void cancel(TestEvent e) { e.setCancelled(true); }

            @PacketListener(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void monitor(TestEvent e) { fired.add("monitor"); }
        });

        manager.fireEvent(new TestEvent("p", "s"));

        assertEquals(List.of("monitor"), fired,
                "MONITOR must always fire regardless of cancellation and ignoreCancelled");
    }

    // -------------------------------------------------------------------------
    // unregisterAll
    // -------------------------------------------------------------------------

    @Test
    void unregisterAllRemovesHandlersForListener() {
        List<String> fired = new ArrayList<>();
        Object listener = new Object() {
            @PacketListener
            public void onEvent(TestEvent e) { fired.add("hit"); }
        };

        manager.registerListener(listener);
        manager.unregisterAll(listener);
        manager.fireEvent(new TestEvent("p", "s"));

        assertTrue(fired.isEmpty(), "Handlers must not fire after unregisterAll");
    }

    @Test
    void unregisterAllUnregisteredListenerIsNoOp() {
        assertDoesNotThrow(() -> manager.unregisterAll(new Object()));
    }

    @Test
    void unregisterAllOnlyRemovesTargetListener() {
        List<String> fired = new ArrayList<>();
        Object toRemove = new Object() {
            @PacketListener
            public void onEvent(TestEvent e) { fired.add("removed"); }
        };
        Object toKeep = new Object() {
            @PacketListener
            public void onEvent(TestEvent e) { fired.add("kept"); }
        };

        manager.registerListener(toRemove);
        manager.registerListener(toKeep);
        manager.unregisterAll(toRemove);
        manager.fireEvent(new TestEvent("p", "s"));

        assertEquals(List.of("kept"), fired);
    }

    // -------------------------------------------------------------------------
    // Invalid signatures
    // -------------------------------------------------------------------------

    @Test
    void registerListenerRejectsZeroParamMethod() {
        Object badListener = new Object() {
            @PacketListener
            public void onEvent() {}
        };

        assertThrows(IllegalArgumentException.class, () -> manager.registerListener(badListener));
    }

    @Test
    void registerListenerRejectsWrongParamType() {
        Object badListener = new Object() {
            @PacketListener
            public void onEvent(String notAnEvent) {}
        };

        assertThrows(IllegalArgumentException.class, () -> manager.registerListener(badListener));
    }

    @Test
    void registerListenerRejectsTwoParams() {
        Object badListener = new Object() {
            @PacketListener
            public void onEvent(TestEvent e, String extra) {}
        };

        assertThrows(IllegalArgumentException.class, () -> manager.registerListener(badListener));
    }

    // -------------------------------------------------------------------------
    // Exception resilience
    // -------------------------------------------------------------------------

    @Test
    void handlerExceptionDoesNotPreventNextHandlerFromFiring() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener(priority = EventPriority.LOW)
            public void throws_(TestEvent e) { throw new RuntimeException("boom"); }

            @PacketListener(priority = EventPriority.NORMAL)
            public void after(TestEvent e) { fired.add("after"); }
        });

        manager.fireEvent(new TestEvent("p", "s"));

        assertEquals(List.of("after"), fired,
                "Next handler must still fire after a previous handler throws");
    }

    // -------------------------------------------------------------------------
    // Null guards
    // -------------------------------------------------------------------------

    @Test
    void constructorRejectsNullLogger() {
        assertThrows(NullPointerException.class, () -> new ProxyEventManager(null));
    }

    @Test
    void registerListenerRejectsNull() {
        assertThrows(NullPointerException.class, () -> manager.registerListener(null));
    }

    @Test
    void unregisterAllRejectsNull() {
        assertThrows(NullPointerException.class, () -> manager.unregisterAll(null));
    }

    @Test
    void fireEventRejectsNull() {
        assertThrows(NullPointerException.class, () -> manager.fireEvent(null));
    }

    // -------------------------------------------------------------------------
    // Additional edge cases
    // -------------------------------------------------------------------------

    @Test
    void listenerWithNoAnnotatedMethodsIsNoOp() {
        // must not throw and must not register any handlers
        assertDoesNotThrow(() -> manager.registerListener(new Object() {
            public void notAnnotated(TestEvent e) {}
        }));
        // firing should also be a no-op (no explosion)
        assertDoesNotThrow(() -> manager.fireEvent(new TestEvent("p", "s")));
    }

    @Test
    void fireEventPassesExactSameEventInstance() {
        List<ProxyPacketEvent> received = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void on(TestEvent e) { received.add(e); }
        });

        TestEvent event = new TestEvent("p", "s");
        manager.fireEvent(event);

        assertEquals(1, received.size());
        assertSame(event, received.get(0), "Handler must receive the exact same event instance");
    }

    @Test
    void twoListenersForSameEventTypeBothFire() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void first(TestEvent e) { fired.add("first"); }
        });
        manager.registerListener(new Object() {
            @PacketListener
            public void second(TestEvent e) { fired.add("second"); }
        });

        manager.fireEvent(new TestEvent("p", "s"));

        assertEquals(2, fired.size(), "Both listeners must fire");
        assertTrue(fired.contains("first"));
        assertTrue(fired.contains("second"));
    }

    @Test
    void firingSubtypeDoesNotMatchHandlerForSupertype() {
        // ProxyEventManager uses exact class matching via Map key
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void onOther(OtherTestEvent e) { fired.add("other"); }
        });

        // fire a TestEvent — the OtherTestEvent handler must not trigger
        manager.fireEvent(new TestEvent("p", "s"));

        assertTrue(fired.isEmpty(), "Handler for OtherTestEvent must not fire on TestEvent");
    }

    // -------------------------------------------------------------------------
    // Minimal test event implementations
    // -------------------------------------------------------------------------

    private static final class TestEvent implements ProxyPacketEvent {
        private final EzContext context;
        private boolean cancelled;

        TestEvent(String playerName, String sourceServer) {
            this.context = new EzContext(playerName, sourceServer, null);
        }

        @Override public EzContext getContext()              { return context; }
        @Override public boolean   isCancelled()             { return cancelled; }
        @Override public void      setCancelled(boolean c)   { this.cancelled = c; }
    }

    private static final class OtherTestEvent implements ProxyPacketEvent {
        private final EzContext context = new EzContext(null, null, null);
        private boolean cancelled;

        @Override public EzContext getContext()              { return context; }
        @Override public boolean   isCancelled()             { return cancelled; }
        @Override public void      setCancelled(boolean c)   { this.cancelled = c; }
    }
}
