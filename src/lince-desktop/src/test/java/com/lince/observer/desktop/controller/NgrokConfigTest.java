package com.lince.observer.desktop.controller;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import com.lince.observer.desktop.component.NgrokConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NgrokConfigTest {

    @Mock
    private NgrokClient ngrokClient;

    @Mock
    private Tunnel tunnel;

    @InjectMocks
    private NgrokConfig ngrokConfig;

    @BeforeEach
    void setUp() {
        // This method is called before each test
        // We can set up common mocking behavior here if needed
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
    }

    @Test
    void closeNgrok_ShouldDisconnectAndKillClient() {
        // Arrange
        when(ngrokClient.connect(any(CreateTunnel.class))).thenReturn(tunnel);
        ngrokConfig.startNgrok(8080); // This sets up the tunnel

        // Act
        ngrokConfig.closeNgrok();

        // Assert
        verify(ngrokClient).disconnect(anyString());
        verify(ngrokClient).kill();
    }

    @Test
    void closeNgrok_ShouldOnlyKillClientWhenTunnelIsNull() {
        // Act
        ngrokConfig.closeNgrok();

        // Assert
        verify(ngrokClient).disconnect(anyString());
        verify(ngrokClient).kill();
    }
}
