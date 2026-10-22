package com.lince.observer.math.service;

import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The legacy kappa migrates two registers and then reads {@code InstrumentoObservacional.getInstance()}, so the
 * whole calculation must hold the {@link LegacyBridge} lock (LO-349).
 */
class StatsServiceImplLockTest {

    @Test
    @Timeout(5)
    void calculateLegacyKappaWaitsForTheLegacyLock() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        LegacyConverterService converter = mock(LegacyConverterService.class);
        when(converter.migrateRegisterToLegacy(any())).thenAnswer(invocation -> {
            entered.countDown();
            return mock(Registro.class);
        });
        LegacyBridge bridge = new LegacyBridge();
        StatsServiceImpl service = new StatsServiceImpl(mock(DataHubService.class), converter, bridge);

        Thread kappa;
        try (LegacyLockHolder ignored = new LegacyLockHolder(bridge)) {
            kappa = new Thread(() -> service.calculateLegacyKappa(UUID.randomUUID(), UUID.randomUUID()), "legacy-kappa");
            kappa.setDaemon(true);
            kappa.start();
            assertFalse(entered.await(150, TimeUnit.MILLISECONDS),
                    "calculateLegacyKappa migrated registers while another thread held the legacy lock");
        }
        assertTrue(entered.await(1, TimeUnit.SECONDS), "calculateLegacyKappa never ran after the lock was released");
        kappa.join(1000);
    }
}
