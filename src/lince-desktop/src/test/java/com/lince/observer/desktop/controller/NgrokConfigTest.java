package com.lince.observer.desktop.controller;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import com.lince.observer.desktop.component.NgrokConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NgrokConfigTest {

    @Mock
    private NgrokClient ngrokClient;

    @Mock
    private Tunnel tunnel;

    private NgrokConfig ngrokConfig;

    @BeforeEach
    void setUp() throws Exception {
        ngrokConfig = new NgrokConfig(ngrokLog -> null);
        ngrokConfig.setToken("test-token");
        // Inject mock ngrokClient to avoid real ngrok process startup
        Field clientField = NgrokConfig.class.getDeclaredField("ngrokClient");
        clientField.setAccessible(true);
        clientField.set(ngrokConfig, ngrokClient);
    }

    @Test
    void startNgrok_ShouldCreateTunnelAndLogPublicUrl() {
        // Arrange
        when(ngrokClient.connect(any(CreateTunnel.class))).thenReturn(tunnel);
        when(tunnel.getPublicUrl()).thenReturn("https://example.ngrok.io");

        // Act
        ngrokConfig.startNgrok(8080);

        // Assert
        verify(ngrokClient).connect(any(CreateTunnel.class));
        verify(tunnel).getPublicUrl();
        assertTrue(ngrokConfig.getPublicUrl().isPresent());
        assertEquals("https://example.ngrok.io", ngrokConfig.getPublicUrl().get());
        assertNull(ngrokConfig.getLastError());
    }

    @Test
    void closeNgrok_ShouldDisconnectAndKillClient() throws Exception {
        // Arrange - inject a tunnel so disconnect path is taken
        Field tunnelField = NgrokConfig.class.getDeclaredField("tunnel");
        tunnelField.setAccessible(true);
        tunnelField.set(ngrokConfig, tunnel);

        // Act
        ngrokConfig.closeNgrok();

        // Assert
        verify(ngrokClient).disconnect(anyString());
        verify(ngrokClient).kill();
        assertTrue(ngrokConfig.getPublicUrl().isEmpty());
    }

    @Test
    void closeNgrok_ShouldOnlyKillClientWhenTunnelIsNull() {
        // Act - tunnel is null
        ngrokConfig.closeNgrok();

        // Assert - should kill but not disconnect when tunnel is null
        verify(ngrokClient, never()).disconnect(anyString());
        verify(ngrokClient).kill();
    }

    @Test
    void startNgrok_ShouldSetLastErrorOnFailure() {
        // Arrange
        when(ngrokClient.connect(any(CreateTunnel.class)))
                .thenThrow(new RuntimeException("The ngrok process was unable to start."));

        // Act
        ngrokConfig.startNgrok(8080);

        // Assert
        assertNotNull(ngrokConfig.getLastError());
        assertTrue(ngrokConfig.getPublicUrl().isEmpty());
    }
}