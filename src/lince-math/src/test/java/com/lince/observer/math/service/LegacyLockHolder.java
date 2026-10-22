package com.lince.observer.math.service;

import com.lince.observer.data.transfer.legacy.LegacyBridge;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test helper: holds the {@link LegacyBridge} lock from another thread until {@link #close()} (LO-349).
 */
final class LegacyLockHolder implements AutoCloseable {

    private final CountDownLatch release = new CountDownLatch(1);
    private final Thread thread;

    LegacyLockHolder(LegacyBridge bridge) throws InterruptedException {
        CountDownLatch held = new CountDownLatch(1);
        thread = new Thread(() -> bridge.withLegacyModel(() -> {
            held.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }), "legacy-lock-holder");
        thread.setDaemon(true);
        thread.start();
        assertTrue(held.await(1, TimeUnit.SECONDS), "lock holder never acquired the legacy lock");
    }

    @Override
    public void close() throws InterruptedException {
        release.countDown();
        thread.join(1000);
    }
}
