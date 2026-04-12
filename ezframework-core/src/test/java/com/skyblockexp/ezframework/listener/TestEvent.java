package com.skyblockexp.ezframework.listener;

/** Minimal non-cancellable event used in {@link ListenerDispatcherTest}. */
class TestEvent {
    final String value;

    TestEvent(String value) {
        this.value = value;
    }
}
