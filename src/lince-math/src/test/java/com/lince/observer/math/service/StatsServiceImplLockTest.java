package com.lince.observer.math.service;

import com.lince.observer.data.transfer.legacy.LegacyBridge;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The legacy kappa converts two registers through {@link LegacyBridge#toLegacy} and then reads the legacy
 * instrument, so the whole calculation, including reading the project from the data hub, must hold the
 * {@link LegacyBridge} lock (LO-349).
 */
class StatsServiceImplLockTest {

    @Test
    @Timeout(5)
    void calculateLegacyKappaWaitsForTheLegacyLock() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        DataHubService dataHub = mock(DataHubService.class);
        when(dataHub.getCriteria()).thenAnswer(invocation -> {
            entered.countDown();
            return FXCollections.observableArrayList();
        });
        LegacyBridge bridge = new LegacyBridge();
        StatsServiceImpl service = new StatsServiceImpl(dataHub, bridge);

        Thread kappa;
        try (LegacyLockHolder ignored = new LegacyLockHolder(bridge)) {
            kappa = new Thread(() -> service.calculateLegacyKappa(UUID.randomUUID(), UUID.randomUUID()), "legacy-kappa");
            kappa.setDaemon(true);
            kappa.start();
            assertFalse(entered.await(150, TimeUnit.MILLISECONDS),
                    "calculateLegacyKappa converted registers while another thread held the legacy lock");
        }
        assertTrue(entered.await(1, TimeUnit.SECONDS), "calculateLegacyKappa never ran after the lock was released");
        kappa.join(1000);
    }
}
