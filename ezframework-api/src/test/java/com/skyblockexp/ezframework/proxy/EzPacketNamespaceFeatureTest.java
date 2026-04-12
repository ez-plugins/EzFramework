package com.skyblockexp.ezframework.proxy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EzPacketNamespaceFeatureTest {

    // -------------------------------------------------------------------------
    // Constructor validation
    // -------------------------------------------------------------------------

    @Test
    void constructorRejectsNull() {
        assertThrows(NullPointerException.class, () -> new EzPacketNamespace(null));
    }

    @Test
    void constructorRejectsBlankNamespace() {
        assertThrows(IllegalArgumentException.class, () -> new EzPacketNamespace(""));
        assertThrows(IllegalArgumentException.class, () -> new EzPacketNamespace("   "));
    }

    @Test
    void constructorRejectsNamespaceContainingColon() {
        assertThrows(IllegalArgumentException.class, () -> new EzPacketNamespace("my:plugin"));
    }

    @Test
    void constructorNormalizesToLowercase() {
        EzPacketNamespace ns = new EzPacketNamespace("EzEconomy");
        assertEquals("ezeconomy", ns.getNamespace());
    }

    @Test
    void constructorValidNameSucceeds() {
        EzPacketNamespace ns = new EzPacketNamespace("ezeconomy");
        assertEquals("ezeconomy", ns.getNamespace());
    }

    // -------------------------------------------------------------------------
    // id()
    // -------------------------------------------------------------------------

    @Test
    void idBuildsFullyQualifiedId() {
        EzPacketNamespace ns = new EzPacketNamespace("ezeconomy");
        assertEquals("ezeconomy:balance.request", ns.id("balance.request"));
    }

    @Test
    void idRejectsNullAction() {
        EzPacketNamespace ns = new EzPacketNamespace("ezeconomy");
        assertThrows(NullPointerException.class, () -> ns.id(null));
    }

    @Test
    void idRejectsBlankAction() {
        EzPacketNamespace ns = new EzPacketNamespace("ezeconomy");
        assertThrows(IllegalArgumentException.class, () -> ns.id(""));
        assertThrows(IllegalArgumentException.class, () -> ns.id("   "));
    }

    @Test
    void idRejectsActionWithColon() {
        EzPacketNamespace ns = new EzPacketNamespace("ezeconomy");
        assertThrows(IllegalArgumentException.class, () -> ns.id("balance:request"));
    }

    @Test
    void idWithMultiplePartsDotSeparated() {
        EzPacketNamespace ns = new EzPacketNamespace("ezshops");
        assertEquals("ezshops:purchase.confirm.v2", ns.id("purchase.confirm.v2"));
    }

    // -------------------------------------------------------------------------
    // owns()
    // -------------------------------------------------------------------------

    @Test
    void ownsTrueForMatchingNamespace() {
        EzPacketNamespace ns = new EzPacketNamespace("ezeconomy");
        assertTrue(ns.owns("ezeconomy:balance.request"));
    }

    @Test
    void ownsFalseForDifferentNamespace() {
        EzPacketNamespace ns = new EzPacketNamespace("ezeconomy");
        assertFalse(ns.owns("ezshops:purchase.confirm"));
    }

    @Test
    void ownsFalseForNull() {
        EzPacketNamespace ns = new EzPacketNamespace("ezeconomy");
        assertFalse(ns.owns(null));
    }

    @Test
    void ownsFalseForPartialMatch() {
        EzPacketNamespace ns = new EzPacketNamespace("ez");
        // "ez" should NOT own "ezeconomy:balance" even though string starts with "ez"
        assertFalse(ns.owns("ezeconomy:balance"), "partial prefix match should not pass owns() check");
    }

    @Test
    void ownsFalseForEmptyString() {
        EzPacketNamespace ns = new EzPacketNamespace("ezeconomy");
        assertFalse(ns.owns(""));
    }

    // -------------------------------------------------------------------------
    // getNamespace()
    // -------------------------------------------------------------------------

    @Test
    void getNamespaceReturnsLowercaseValue() {
        EzPacketNamespace ns = new EzPacketNamespace("MyPlugin");
        assertEquals("myplugin", ns.getNamespace());
    }

    // -------------------------------------------------------------------------
    // equals() / hashCode()
    // -------------------------------------------------------------------------

    @Test
    void equalNamespacesAreEqual() {
        assertEquals(new EzPacketNamespace("ezeconomy"), new EzPacketNamespace("ezeconomy"));
        assertEquals(new EzPacketNamespace("ezeconomy").hashCode(), new EzPacketNamespace("ezeconomy").hashCode());
    }

    @Test
    void differentNamespacesAreNotEqual() {
        assertNotEquals(new EzPacketNamespace("a"), new EzPacketNamespace("b"));
    }
}
