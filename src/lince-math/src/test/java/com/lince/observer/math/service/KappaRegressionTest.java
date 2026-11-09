package com.lince.observer.math.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.KeyValue;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.component.LinceFileImporter;
import com.lince.observer.data.service.SessionService;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

/**
 * Kappa regression for LO-349 (FR-6, AC-4): the legacy kappa of two observers of {@code kappa/twoObservers.xml}
 * must match the values the former {@code LegacyConverterService} produced before kappa moved to
 * {@link LegacyBridge}, recorded in {@code kappa/expected.json}.
 * Never edit the expected values to make this test pass; a difference is a behaviour change to explain.
 * <p>
 * When {@code expected.json} is missing the test records it and fails, so a deleted fixture is never silently
 * replaced.
 */
class KappaRegressionTest {

    private static final Path EXPECTED = Path.of("src/test/resources/kappa/expected.json");
    private static final double TOLERANCE = 1e-9;

    @Test
    void legacyKappaMatchesTheRecordedValues() throws Exception {
        ILinceProject project = new LinceFileImporter().importLinceProject(new File(Objects.requireNonNull(
                getClass().getClassLoader().getResource("kappa/twoObservers.xml")).getFile()));
        List<LinceRegisterWrapper> registers = project.getRegister();
        assertTrue(registers.size() >= 2, "fixture needs two observers");
        DataHubService dataHubService = new DataHubService(mock(SessionService.class), mock(HttpServletRequest.class));
        dataHubService.setLinceProject(project);

        List<KeyValue<String, Double>> kappa = newStatsService(dataHubService)
                .calculateLegacyKappa(registers.get(0).getId(), registers.get(1).getId());
        assertNotNull(kappa, "calculateLegacyKappa failed, see the log");

        Map<String, Double> actual = new LinkedHashMap<>();
        kappa.forEach(kv -> actual.put(kv.key, kv.value));
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        if (!Files.exists(EXPECTED)) {
            Files.createDirectories(EXPECTED.getParent());
            mapper.writeValue(EXPECTED.toFile(), actual);
            fail("Recorded " + EXPECTED + " from the current converter; review it and run the test again");
        }
        Map<String, Double> expected = mapper.readValue(EXPECTED.toFile(), new TypeReference<LinkedHashMap<String, Double>>() {
        });

        assertEquals(new ArrayList<>(expected.keySet()), new ArrayList<>(actual.keySet()), "criteria and order");
        List<String> differences = new ArrayList<>();
        expected.forEach((key, value) -> {
            Double now = actual.get(key);
            if (now == null || Math.abs(now - value) > TOLERANCE) {
                differences.add(key + ": expected " + value + " but was " + now);
            }
        });
        assertTrue(differences.isEmpty(), "kappa changed: " + differences);
        assertTrue(expected.values().stream().anyMatch(v -> v != 0.0 && v != 1.0),
                "fixture is degenerate: every kappa is 0 or 1");
    }

    private static StatsServiceImpl newStatsService(DataHubService dataHubService) {
        return new StatsServiceImpl(dataHubService, new LegacyBridge());
    }
}
