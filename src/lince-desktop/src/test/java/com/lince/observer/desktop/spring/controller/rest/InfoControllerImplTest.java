package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.info.ApplicationHealth;
import com.lince.observer.data.bean.info.ApplicationInfo;
import com.lince.observer.data.bean.info.ApplicationTarget;
import com.lince.observer.data.service.ApplicationInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class InfoControllerImplTest {

    @Mock
    private ApplicationInfoService applicationInfoService;

    @InjectMocks
    private InfoControllerImpl controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getInfoReturnsServiceValue() {
        ApplicationInfo expected = new ApplicationInfo("Lince PLUS", "4.1.1-SNAPSHOT", null, null, ApplicationTarget.DESKTOP);
        when(applicationInfoService.getApplicationInfo()).thenReturn(expected);

        ResponseEntity<ApplicationInfo> response = controller.getInfo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void getInfoReturns500WhenServiceFails() {
        when(applicationInfoService.getApplicationInfo()).thenThrow(new IllegalStateException("boom"));

        ResponseEntity<ApplicationInfo> response = controller.getInfo();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getHealthReturnsUp() {
        when(applicationInfoService.getHealth()).thenReturn(ApplicationHealth.up(ApplicationTarget.DESKTOP));

        ResponseEntity<ApplicationHealth> response = controller.getHealth();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ApplicationHealth.STATUS_UP, response.getBody().status());
    }

    @Test
    void getHealthReturns500WhenServiceFails() {
        when(applicationInfoService.getHealth()).thenThrow(new IllegalStateException("boom"));

        ResponseEntity<ApplicationHealth> response = controller.getHealth();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
