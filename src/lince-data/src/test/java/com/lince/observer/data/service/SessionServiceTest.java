package com.lince.observer.data.service;

import com.lince.observer.data.common.SessionDataAttributes;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @Test
    void testSetSessionDataWithValidInputs() {
        // Arrange
        SessionService sessionService = new SessionService();
        HttpSession httpSession = new MockHttpSession();
        SessionDataAttributes key = SessionDataAttributes.OBSERVER_NAME;
        String value = "observer";

        // Act
        boolean result = sessionService.setSessionData(httpSession, key, value);

        // Assert
        assertTrue(result);
        assertEquals(value, httpSession.getAttribute(key.getItemLabel()));
    }

    @Test
    void testSetSessionDataWithNullSession() {
        // Arrange
        SessionService sessionService = new SessionService();
        HttpSession httpSession = null;
        SessionDataAttributes key = SessionDataAttributes.OBSERVER_NAME;
        String value = "observer";

        // Act
        boolean result = sessionService.setSessionData(httpSession, key, value);

        // Assert
        assertFalse(result);
    }


    @Test
    void testSetSessionDataWithException() {
        // Arrange
        SessionService sessionService = new SessionService();
        HttpSession httpSession = new HttpSessionThrowingException();
        SessionDataAttributes key = SessionDataAttributes.OBSERVER_NAME;
        String value = "observer";

        // Act
        boolean result = sessionService.setSessionData(httpSession, key, value);

        // Assert
        assertFalse(result);
    }

    /**
     * A HttpSession implementation that throws exception for setAttribute.
     */
    private static class HttpSessionThrowingException extends MockHttpSession {
        @Override
        public void setAttribute(String name, Object value) {
            throw new RuntimeException("Exception thrown for testing.");
        }
    }
}
