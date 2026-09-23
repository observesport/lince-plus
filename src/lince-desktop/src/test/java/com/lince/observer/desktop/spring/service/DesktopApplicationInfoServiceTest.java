package com.lince.observer.desktop.spring.service;

import com.lince.observer.data.bean.info.ApplicationHealth;
import com.lince.observer.data.bean.info.ApplicationInfo;
import com.lince.observer.data.bean.info.ApplicationTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DesktopApplicationInfoServiceTest {

    private final Environment environment = mock(Environment.class);
    @SuppressWarnings("unchecked")
    private final ObjectProvider<BuildProperties> buildProvider = mock(ObjectProvider.class);

    @BeforeEach
    void setUp() {
        when(buildProvider.getIfAvailable()).thenReturn(null);
    }

    private void property(String key, String value) {
        when(environment.getProperty(key)).thenReturn(value);
    }

    private DesktopApplicationInfoService service() {
        return new DesktopApplicationInfoService(environment, buildProvider);
    }

    private static BuildProperties buildProperties(String version, String time) {
        Properties p = new Properties();
        if (version != null) p.setProperty("version", version);
        if (time != null) p.setProperty("time", time);
        return new BuildProperties(p);
    }

    @Test
    void usesFilteredAppVersionProperty() {
        property("app.version", "4.1.1-SNAPSHOT");
        property("app.ui.title", "Lince PLUS Software for observation");

        ApplicationInfo info = service().getApplicationInfo();

        assertEquals("4.1.1-SNAPSHOT", info.version());
        assertEquals("Lince PLUS Software for observation", info.name());
        assertEquals(ApplicationTarget.DESKTOP, info.target());
        assertNull(info.build());
        assertNull(info.buildTime());
    }

    @Test
    void fallsBackToBuildPropertiesWhenPlaceholderIsUnfiltered() {
        property("app.version", "@version@");
        when(buildProvider.getIfAvailable()).thenReturn(buildProperties("4.1.1-SNAPSHOT", "2026-09-23T10:00:00Z"));

        ApplicationInfo info = service().getApplicationInfo();

        assertEquals("4.1.1-SNAPSHOT", info.version());
        assertEquals("2026-09-23T10:00:00Z", info.buildTime());
    }

    @Test
    void prefersPropertyOverBuildPropertiesButKeepsBuildTime() {
        property("app.version", "4.2.0");
        when(buildProvider.getIfAvailable()).thenReturn(buildProperties("4.1.1-SNAPSHOT", "2026-09-23T10:00:00Z"));

        ApplicationInfo info = service().getApplicationInfo();

        assertEquals("4.2.0", info.version());
        assertEquals("2026-09-23T10:00:00Z", info.buildTime());
    }

    @Test
    void reportsUnknownVersionAsNull() {
        property("app.version", "  ");

        ApplicationInfo info = service().getApplicationInfo();

        assertNull(info.version());
        assertEquals(DesktopApplicationInfoService.DEFAULT_NAME, info.name());
    }

    @Test
    void healthIsUpForDesktop() {
        ApplicationHealth health = service().getHealth();

        assertEquals(ApplicationHealth.STATUS_UP, health.status());
        assertEquals(ApplicationTarget.DESKTOP, health.target());
    }
}
