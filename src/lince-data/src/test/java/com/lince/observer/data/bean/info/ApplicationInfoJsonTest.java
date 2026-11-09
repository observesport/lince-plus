package com.lince.observer.data.bean.info;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationInfoJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serialisesTargetLowercaseAndOmitsUnknownFields() throws Exception {
        ApplicationInfo info = new ApplicationInfo("Lince PLUS", "4.1.1-SNAPSHOT", null, null, ApplicationTarget.DESKTOP);

        String json = mapper.writeValueAsString(info);

        assertTrue(json.contains("\"target\":\"desktop\""), json);
        assertTrue(json.contains("\"version\":\"4.1.1-SNAPSHOT\""), json);
        assertFalse(json.contains("build"), json);
    }

    @Test
    void roundTripsThroughJackson() throws Exception {
        ApplicationInfo info = new ApplicationInfo("Lince", "1.0", "abc123", "2026-09-23T10:00:00Z", ApplicationTarget.CLOUD);

        ApplicationInfo back = mapper.readValue(mapper.writeValueAsString(info), ApplicationInfo.class);

        assertEquals(info, back);
    }

    @Test
    void healthUpCarriesTarget() throws Exception {
        String json = mapper.writeValueAsString(ApplicationHealth.up(ApplicationTarget.DESKTOP));

        assertEquals("{\"status\":\"UP\",\"target\":\"desktop\"}", json);
    }
}
