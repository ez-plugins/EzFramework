package com.skyblockexp.ezframework.proxy.event;

import com.skyblockexp.ezframework.proxy.EzContext;
import com.skyblockexp.ezframework.proxy.EzMessenger;
import com.skyblockexp.ezframework.proxy.EzPacket;
import com.skyblockexp.ezframework.proxy.EzPacketHandler;
import com.skyblockexp.ezframework.proxy.ServerMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class BridgeEventFeatureTest {

    private static final Logger SILENT_LOGGER = Logger.getLogger("test-bridge-silent");

    private InMemoryMessenger messenger;
    private ProxyEventManager manager;

    @BeforeEach
    void setUp() {
        messenger = new InMemoryMessenger();
        manager = new ProxyEventManager(SILENT_LOGGER);
    }

    // -------------------------------------------------------------------------
    // Basic wiring
    // -------------------------------------------------------------------------

    @Test
    void bridgeRegistersHandlerForPacketType() {
        messenger.bridgeEvent(LoginPacket.class, LoginEvent::new, manager);
        assertTrue(messenger.hasHandlerFor(new LoginPacket("Alex")),
                "bridgeEvent must register a handler for the given packet type");
    }

    @Test
    void bridgeDispatchFiresListenerOnManager() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void onLogin(LoginEvent e) { fired.add(e.getLoginName()); }
        });

        messenger.bridgeEvent(LoginPacket.class, LoginEvent::new, manager);
        messenger.simulateDispatch(new LoginPacket("Alex"), new EzContext("Alex", "lobby", null));

        assertEquals(List.of("Alex"), fired);
    }

    @Test
    void bridgeFactoryReceivesCorrectPacket() {
        List<LoginPacket> received = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void on(LoginEvent e) { received.add(e.originalPacket()); }
        });

        LoginPacket packet = new LoginPacket("Bob");
        messenger.bridgeEvent(LoginPacket.class,
                (p, ctx) -> new LoginEvent(p, ctx),
                manager);
        messenger.simulateDispatch(packet, new EzContext("Bob", "hub", null));

        assertEquals(1, received.size());
        assertSame(packet, received.get(0));
    }

    @Test
    void bridgeFactoryReceivesCorrectContext() {
        List<EzContext> received = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void on(LoginEvent e) { received.add(e.getContext()); }
        });

        EzContext ctx = new EzContext("Carol", "survival", null);
        messenger.bridgeEvent(LoginPacket.class, LoginEvent::new, manager);
        messenger.simulateDispatch(new LoginPacket("Carol"), ctx);

        assertEquals(1, received.size());
        assertSame(ctx, received.get(0));
    }

    // -------------------------------------------------------------------------
    // Cancellation propagates through the bridge
    // -------------------------------------------------------------------------

    @Test
    void cancelledEventIsReflectedInFiredEvent() {
        manager.registerListener(new Object() {
            @PacketListener(priority = EventPriority.LOW)
            public void cancel(LoginEvent e) { e.setCancelled(true); }
        });

        messenger.bridgeEvent(LoginPacket.class, LoginEvent::new, manager);
        LoginEvent[] captured = new LoginEvent[1];

        manager.registerListener(new Object() {
            @PacketListener(priority = EventPriority.MONITOR)
            public void monitor(LoginEvent e) { captured[0] = e; }
        });

        messenger.simulateDispatch(new LoginPacket("Dave"), new EzContext("Dave", "prison", null));

        assertNotNull(captured[0]);
        assertTrue(captured[0].isCancelled());
    }

    // -------------------------------------------------------------------------
    // Multiple packet types bridged to the same manager
    // -------------------------------------------------------------------------

    @Test
    void twoBridgesForDifferentPacketTypesBothFire() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void onLogin(LoginEvent e)  { fired.add("login:"  + e.getLoginName()); }

            @PacketListener
            public void onLogout(LogoutEvent e) { fired.add("logout:" + e.getPlayerName()); }
        });

        messenger.bridgeEvent(LoginPacket.class,  LoginEvent::new,  manager);
        messenger.bridgeEvent(LogoutPacket.class, LogoutEvent::new, manager);

        messenger.simulateDispatch(new LoginPacket("Eve"),   new EzContext("Eve",  "lobby", null));
        messenger.simulateDispatch(new LogoutPacket("Frank"), new EzContext("Frank", "lobby", null));

        assertEquals(List.of("login:Eve", "logout:Frank"), fired);
    }

    @Test
    void bridgingOneTypeDoesNotFireHandlerForOtherType() {
        List<String> fired = new ArrayList<>();
        manager.registerListener(new Object() {
            @PacketListener
            public void onLogin(LoginEvent e) { fired.add("login"); }
        });

        messenger.bridgeEvent(LoginPacket.class, LoginEvent::new, manager);
        // dispatch a LogoutPacket — no bridge registered for it
        messenger.simulateDispatch(new LogoutPacket("Grace"), new EzContext("Grace", "game", null));

        assertTrue(fired.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Null guards (fail-fast at bridgeEvent call time)
    // -------------------------------------------------------------------------

    @Test
    void bridgeNullPacketTypeThrows() {
        assertThrows(NullPointerException.class,
                () -> messenger.bridgeEvent(null, LoginEvent::new, manager));
    }

    @Test
    void bridgeNullFactoryThrows() {
        assertThrows(NullPointerException.class,
                () -> messenger.bridgeEvent(LoginPacket.class, null, manager));
    }

    @Test
    void bridgeNullManagerThrows() {
        assertThrows(NullPointerException.class,
                () -> messenger.bridgeEvent(LoginPacket.class, LoginEvent::new, null));
    }

    // -------------------------------------------------------------------------
    // Minimal in-memory EzMessenger stub
    // -------------------------------------------------------------------------

    /**
     * A minimal, synchronous, in-memory {@link EzMessenger} implementation used
     * only to test {@link EzMessenger#bridgeEvent}). {@link #send} and
     * {@link #broadcast} are intentional no-ops.
     */
    private static final class InMemoryMessenger implements EzMessenger {

        private final Map<String, EzPacketHandler<?>> handlers = new ConcurrentHashMap<>();

        @Override
        public void send(String server, ServerMessage message) { /* no-op in tests */ }

        @Override
        public void broadcast(ServerMessage message) { /* no-op in tests */ }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends EzPacket> void registerHandler(Class<T> packetType, EzPacketHandler<T> handler) {
            Objects.requireNonNull(packetType, "packetType");
            Objects.requireNonNull(handler, "handler");
            try {
                T instance = packetType.getDeclaredConstructor().newInstance();
                handlers.put(instance.packetId(), handler);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(
                        "Cannot register handler for " + packetType.getName()
                                + ": requires a public no-arg constructor", e);
            }
        }

        /** Returns {@code true} if a handler has been registered for the given packet's type. */
        boolean hasHandlerFor(EzPacket packet) {
            return handlers.containsKey(packet.packetId());
        }

        /** Simulate an inbound packet arriving on this messenger. */
        @SuppressWarnings({"unchecked", "rawtypes"})
        <T extends EzPacket> void simulateDispatch(T packet, EzContext context) {
            EzPacketHandler handler = handlers.get(packet.packetId());
            if (handler != null) {
                handler.handle(packet, context);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Test packet + event implementations
    // -------------------------------------------------------------------------

    public static final class LoginPacket implements EzPacket {
        private final String name;
        public LoginPacket() { this.name = ""; }
        public LoginPacket(String name) { this.name = name; }
        @Override public String packetId() { return "test:login"; }
        public String getName() { return name; }
    }

    public static final class LogoutPacket implements EzPacket {
        private final String name;
        public LogoutPacket() { this.name = ""; }
        public LogoutPacket(String name) { this.name = name; }
        @Override public String packetId() { return "test:logout"; }
        public String getName() { return name; }
    }

    private static final class LoginEvent implements ProxyPacketEvent {
        private final LoginPacket packet;
        private final EzContext context;
        private boolean cancelled;

        LoginEvent(LoginPacket packet, EzContext context) {
            this.packet = packet;
            this.context = context;
        }

        public String getLoginName() { return packet.getName(); }
        public LoginPacket originalPacket() { return packet; }

        @Override public EzContext getContext()            { return context; }
        @Override public boolean   isCancelled()           { return cancelled; }
        @Override public void      setCancelled(boolean c) { this.cancelled = c; }
    }

    private static final class LogoutEvent implements ProxyPacketEvent {
        private final LogoutPacket packet;
        private final EzContext context;
        private boolean cancelled;

        LogoutEvent(LogoutPacket packet, EzContext context) {
            this.packet = packet;
            this.context = context;
        }

        public String getPlayerName() { return packet.getName(); }

        @Override public EzContext getContext()            { return context; }
        @Override public boolean   isCancelled()           { return cancelled; }
        @Override public void      setCancelled(boolean c) { this.cancelled = c; }
    }
}
