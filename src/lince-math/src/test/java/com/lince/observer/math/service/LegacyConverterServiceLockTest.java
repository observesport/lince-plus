package com.lince.observer.math.service;

import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link LegacyConverterService} replaces the legacy singletons, so it must hold the {@link LegacyBridge} lock
 * that the legacy importers and exporters use (LO-349).
 */
class LegacyConverterServiceLockTest {

    @Test
    @Timeout(5)
    void migrateDataToLegacyWaitsForTheLegacyLock() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CategoryService categoryService = mock(CategoryService.class);
        AnalysisService analysisService = mock(AnalysisService.class);
        when(categoryService.getObservationTool()).thenAnswer(invocation -> {
            entered.countDown();
            return List.of();
        });
        when(analysisService.getObservationById(any())).thenReturn(List.of());
        LegacyBridge bridge = new LegacyBridge();
        LegacyConverterService service = new LegacyConverterService(categoryService, analysisService, bridge);

        Thread migration;
        try (LegacyLockHolder ignored = new LegacyLockHolder(bridge)) {
            migration = new Thread(() -> service.migrateDataToLegacy(UUID.randomUUID()), "legacy-migration");
            migration.setDaemon(true);
            migration.start();
            assertFalse(entered.await(150, TimeUnit.MILLISECONDS),
                    "migrateDataToLegacy replaced the legacy singletons while another thread held the lock");
        }
        assertTrue(entered.await(1, TimeUnit.SECONDS), "migrateDataToLegacy never ran after the lock was released");
        migration.join(1000);
    }
}
