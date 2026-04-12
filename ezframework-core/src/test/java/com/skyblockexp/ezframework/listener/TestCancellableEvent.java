package com.skyblockexp.ezframework.listener;

/** Cancellable event used in {@link ListenerDispatcherTest}. */
class TestCancellableEvent implements Cancellable {
    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
