package com.skyblockexp.ezframework.proxy.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventPriorityFeatureTest {

    // -------------------------------------------------------------------------
    // Membership
    // -------------------------------------------------------------------------

    @Test
    void sixValuesExist() {
        assertEquals(6, EventPriority.values().length);
    }

    @Test
    void allExpectedValuesPresent() {
        assertDoesNotThrow(() -> {
            EventPriority.valueOf("LOWEST");
            EventPriority.valueOf("LOW");
            EventPriority.valueOf("NORMAL");
            EventPriority.valueOf("HIGH");
            EventPriority.valueOf("HIGHEST");
            EventPriority.valueOf("MONITOR");
        });
    }

    // -------------------------------------------------------------------------
    // Ordinal ordering — drives dispatch order in ProxyEventManager
    // -------------------------------------------------------------------------

    @Test
    void lowestHasOrdinalZero() {
        assertEquals(0, EventPriority.LOWEST.ordinal());
    }

    @Test
    void monitorHasHighestOrdinal() {
        assertEquals(5, EventPriority.MONITOR.ordinal());
    }

    @Test
    void ordinalsAreStrictlyAscending() {
        EventPriority[] values = EventPriority.values();
        for (int i = 0; i < values.length - 1; i++) {
            assertTrue(
                    values[i].ordinal() < values[i + 1].ordinal(),
                    values[i].name() + " ordinal must be less than " + values[i + 1].name());
        }
    }

    @Test
    void lowOrdinalIsLessThanHighOrdinal() {
        assertTrue(EventPriority.LOW.ordinal() < EventPriority.HIGH.ordinal());
    }

    @Test
    void normalOrdinalIsLessThanHighestOrdinal() {
        assertTrue(EventPriority.NORMAL.ordinal() < EventPriority.HIGHEST.ordinal());
    }

    @Test
    void highestOrdinalIsLessThanMonitorOrdinal() {
        assertTrue(EventPriority.HIGHEST.ordinal() < EventPriority.MONITOR.ordinal());
    }

    // -------------------------------------------------------------------------
    // Name round-trip
    // -------------------------------------------------------------------------

    @Test
    void valueOfByNameReturnsCorrectConstant() {
        assertSame(EventPriority.NORMAL, EventPriority.valueOf("NORMAL"));
    }

    @Test
    void valueOfUnknownNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> EventPriority.valueOf("UNKNOWN"));
    }

    // -------------------------------------------------------------------------
    // values() ordering matches intended dispatch order
    // -------------------------------------------------------------------------

    @Test
    void valuesReturnedInDispatchOrder() {
        EventPriority[] expected = {
            EventPriority.LOWEST,
            EventPriority.LOW,
            EventPriority.NORMAL,
            EventPriority.HIGH,
            EventPriority.HIGHEST,
            EventPriority.MONITOR
        };
        assertArrayEquals(expected, EventPriority.values());
    }
}
