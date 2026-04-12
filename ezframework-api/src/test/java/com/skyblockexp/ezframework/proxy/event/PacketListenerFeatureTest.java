package com.skyblockexp.ezframework.proxy.event;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class PacketListenerFeatureTest {

    // -------------------------------------------------------------------------
    // Default attribute values
    // -------------------------------------------------------------------------

    @Test
    void defaultPriorityIsNormal() throws NoSuchMethodException {
        Method m = Annotated.class.getMethod("withDefaults");
        PacketListener ann = m.getAnnotation(PacketListener.class);
        assertEquals(EventPriority.NORMAL, ann.priority());
    }

    @Test
    void defaultIgnoreCancelledIsFalse() throws NoSuchMethodException {
        Method m = Annotated.class.getMethod("withDefaults");
        PacketListener ann = m.getAnnotation(PacketListener.class);
        assertFalse(ann.ignoreCancelled());
    }

    // -------------------------------------------------------------------------
    // Custom attribute values round-trip
    // -------------------------------------------------------------------------

    @Test
    void customPriorityIsStored() throws NoSuchMethodException {
        Method m = Annotated.class.getMethod("withHighest");
        PacketListener ann = m.getAnnotation(PacketListener.class);
        assertEquals(EventPriority.HIGHEST, ann.priority());
    }

    @Test
    void customIgnoreCancelledTrueIsStored() throws NoSuchMethodException {
        Method m = Annotated.class.getMethod("withIgnoreCancelled");
        PacketListener ann = m.getAnnotation(PacketListener.class);
        assertTrue(ann.ignoreCancelled());
    }

    @Test
    void monitorPriorityIsStored() throws NoSuchMethodException {
        Method m = Annotated.class.getMethod("withMonitor");
        PacketListener ann = m.getAnnotation(PacketListener.class);
        assertEquals(EventPriority.MONITOR, ann.priority());
    }

    // -------------------------------------------------------------------------
    // Meta-annotations: retention and target
    // -------------------------------------------------------------------------

    @Test
    void annotationIsRetainedAtRuntime() {
        Retention ret = PacketListener.class.getAnnotation(Retention.class);
        assertNotNull(ret, "@Retention must be declared on @PacketListener");
        assertEquals(RetentionPolicy.RUNTIME, ret.value());
    }

    @Test
    void annotationTargetIsMethodOnly() {
        Target target = PacketListener.class.getAnnotation(Target.class);
        assertNotNull(target, "@Target must be declared on @PacketListener");
        ElementType[] targets = target.value();
        assertEquals(1, targets.length, "@PacketListener must target exactly one element type");
        assertEquals(ElementType.METHOD, targets[0]);
    }

    // -------------------------------------------------------------------------
    // Discoverability via reflection (mirrors what ProxyEventManager does)
    // -------------------------------------------------------------------------

    @Test
    void annotationIsDiscoverableViaGetMethods() throws NoSuchMethodException {
        Method m = Annotated.class.getMethod("withDefaults");
        assertNotNull(m.getAnnotation(PacketListener.class));
    }

    @Test
    void unannotatedMethodReturnsNullAnnotation() throws NoSuchMethodException {
        Method m = Annotated.class.getMethod("notAnnotated");
        assertNull(m.getAnnotation(PacketListener.class));
    }

    // -------------------------------------------------------------------------
    // Helper class carrying various annotation configurations
    // -------------------------------------------------------------------------

    public static class Annotated {

        @PacketListener
        public void withDefaults() {}

        @PacketListener(priority = EventPriority.HIGHEST)
        public void withHighest() {}

        @PacketListener(ignoreCancelled = true)
        public void withIgnoreCancelled() {}

        @PacketListener(priority = EventPriority.MONITOR)
        public void withMonitor() {}

        public void notAnnotated() {}
    }
}
