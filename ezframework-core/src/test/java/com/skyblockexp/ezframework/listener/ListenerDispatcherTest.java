package com.skyblockexp.ezframework.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListenerDispatcherTest {

    private ListenerDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new ListenerDispatcher();
    }

    // -----------------------------------------------------------------------
    // Basic dispatch
    // -----------------------------------------------------------------------

    @Test
    void fire_invokesMatchingHandler() {
        List<String> received = new ArrayList<>();

        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener
            public void onEvent(TestEvent event) {
                received.add(event.value);
            }
        });

        dispatcher.fire(new TestEvent("hello"));

        assertEquals(List.of("hello"), received);
    }

    @Test
    void fire_doesNotInvokeHandlerForDifferentEventType() {
        List<String> received = new ArrayList<>();

        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener
            public void onEvent(TestEvent event) {
                received.add(event.value);
            }
        });

        dispatcher.fire(new TestCancellableEvent());

        assertTrue(received.isEmpty());
    }

    @Test
    void fire_returnsEventInstance() {
        TestEvent event = new TestEvent("x");
        TestEvent returned = dispatcher.fire(event);
        assertSame(event, returned);
    }

    @Test
    void fire_noHandlersRegistered_returnsEventUnchanged() {
        TestEvent event = new TestEvent("x");
        assertSame(event, dispatcher.fire(event));
    }

    // -----------------------------------------------------------------------
    // Priority ordering
    // -----------------------------------------------------------------------

    @Test
    void fire_invokesHandlersInPriorityOrder() {
        List<String> order = new ArrayList<>();

        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.HIGH)
            public void high(TestEvent e) { order.add("HIGH"); }
        });
        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.LOWEST)
            public void lowest(TestEvent e) { order.add("LOWEST"); }
        });
        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.NORMAL)
            public void normal(TestEvent e) { order.add("NORMAL"); }
        });
        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.MONITOR)
            public void monitor(TestEvent e) { order.add("MONITOR"); }
        });

        dispatcher.fire(new TestEvent(""));

        assertEquals(List.of("LOWEST", "NORMAL", "HIGH", "MONITOR"), order);
    }

    // -----------------------------------------------------------------------
    // ignoreCancelled
    // -----------------------------------------------------------------------

    @Test
    void fire_ignoreCancelled_skipsHandlerWhenCancelled() {
        List<String> received = new ArrayList<>();

        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.LOW)
            public void canceller(TestCancellableEvent e) {
                e.setCancelled(true);
            }
        });
        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.NORMAL, ignoreCancelled = true)
            public void skipped(TestCancellableEvent e) {
                received.add("should-not-run");
            }
        });

        dispatcher.fire(new TestCancellableEvent());

        assertTrue(received.isEmpty(), "ignoreCancelled handler should be skipped");
    }

    @Test
    void fire_ignoreCancelledFalse_stillRunsWhenCancelled() {
        List<String> received = new ArrayList<>();

        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.LOW)
            public void canceller(TestCancellableEvent e) {
                e.setCancelled(true);
            }
        });
        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.NORMAL)
            public void alwaysRuns(TestCancellableEvent e) {
                received.add("ran");
            }
        });

        dispatcher.fire(new TestCancellableEvent());

        assertEquals(List.of("ran"), received);
    }

    @Test
    void fire_ignoreCancelled_runsNormallyWhenNotCancelled() {
        List<String> received = new ArrayList<>();

        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(ignoreCancelled = true)
            public void handler(TestCancellableEvent e) {
                received.add("ran");
            }
        });

        dispatcher.fire(new TestCancellableEvent()); // not cancelled

        assertEquals(List.of("ran"), received);
    }

    // -----------------------------------------------------------------------
    // Multiple listeners / handler count
    // -----------------------------------------------------------------------

    @Test
    void registerListener_multipleListeners_allInvoked() {
        List<String> received = new ArrayList<>();

        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener
            public void first(TestEvent e) { received.add("first"); }
        });
        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener
            public void second(TestEvent e) { received.add("second"); }
        });

        dispatcher.fire(new TestEvent(""));

        assertEquals(2, received.size());
        assertTrue(received.containsAll(List.of("first", "second")));
    }

    @Test
    void handlerCount_returnsCorrectCount() {
        assertEquals(0, dispatcher.handlerCount(TestEvent.class));

        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener
            public void a(TestEvent e) {}

            @PacketListener(priority = EventPriority.HIGH)
            public void b(TestEvent e) {}
        });

        assertEquals(2, dispatcher.handlerCount(TestEvent.class));
    }

    // -----------------------------------------------------------------------
    // Edge cases
    // -----------------------------------------------------------------------

    @Test
    void registerListener_methodWithNoAnnotation_isIgnored() {
        dispatcher.registerListener(new PacketListenerInterface() {
            public void notAHandler(TestEvent e) {}
        });

        assertEquals(0, dispatcher.handlerCount(TestEvent.class));
    }

    @Test
    void registerListener_methodWithWrongParamCount_isIgnored() {
        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener
            public void twoParams(TestEvent e, String extra) {}
        });

        assertEquals(0, dispatcher.handlerCount(TestEvent.class));
    }

    @Test
    void fire_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> dispatcher.fire(null));
    }

    @Test
    void registerListener_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> dispatcher.registerListener(null));
    }

    @Test
    void fire_handlerThrowsException_continuesWithNextHandler() {
        List<String> received = new ArrayList<>();

        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.LOW)
            public void thrower(TestEvent e) {
                throw new RuntimeException("intentional");
            }
        });
        dispatcher.registerListener(new PacketListenerInterface() {
            @PacketListener(priority = EventPriority.NORMAL)
            public void survivor(TestEvent e) {
                received.add("ran");
            }
        });

        assertDoesNotThrow(() -> dispatcher.fire(new TestEvent("")));
        assertEquals(List.of("ran"), received);
    }
}
