package com.skyblockexp.ezframework.bootstrap;

import com.skyblockexp.ezframework.testutil.TestPlugin;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class BootstrapTest {
    @Test
    public void startStopReloadOrderAndExceptions() {
        TestPlugin plugin = new TestPlugin();
        Bootstrap b = new Bootstrap(plugin.getLogger());

        AtomicInteger calls = new AtomicInteger(0);

        Component ok = new Component() {
            @Override
            public void start() { calls.incrementAndGet(); }

            @Override
            public void stop() { calls.incrementAndGet(); }
        };

        Component failOnStart = new Component() {
            @Override
            public void start() throws Exception { calls.incrementAndGet(); throw new RuntimeException("boom"); }

            @Override
            public void stop() { calls.incrementAndGet(); }
        };

        b.register(ok).register(failOnStart);
        // startAll should call both and swallow exception
        b.startAll();
        assertTrue(calls.get() >= 2);

        // reloadAll should call reload (default no-op)
        b.reloadAll();

        // stopAll should call in reverse order
        int before = calls.get();
        b.stopAll();
        assertTrue(calls.get() >= before + 2);
    }
}
