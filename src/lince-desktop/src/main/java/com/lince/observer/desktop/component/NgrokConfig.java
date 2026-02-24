package com.lince.observer.desktop.component;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.installer.NgrokVersion;
import com.github.alexdlaird.ngrok.process.NgrokLog;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

public class NgrokConfig {
    private static final Logger log = LoggerFactory.getLogger(NgrokConfig.class);
    private NgrokClient ngrokClient;
    private Tunnel tunnel;
    private String token;
    private String lastError;
    private final Function<NgrokLog, Void> logEventCallback;

    public NgrokConfig(Function<NgrokLog, Void> logEventCallback) {
        this.logEventCallback = logEventCallback;
    }

    private void lazyInit() {
        if (ngrokClient == null) {
            if (StringUtils.isEmpty(token)) {
                throw new IllegalStateException("Ngrok token is not set");
            }
            final JavaNgrokConfig javaNgrokConfig = new JavaNgrokConfig.Builder()
                    .withAuthToken(token)
                    .withLogEventCallback(logEventCallback)
                    .withMaxLogs(10)
                    .withNgrokVersion(NgrokVersion.V3).build();
            this.ngrokClient = new NgrokClient.Builder()
                    .withJavaNgrokConfig(javaNgrokConfig)
                    .build();
        }
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void startNgrok(Integer port) {
        try {
            lazyInit();
            CreateTunnel createTunnel = new CreateTunnel.Builder()
                    .withAddr(port)
                    .build();
            this.tunnel = ngrokClient.connect(createTunnel);
            log.info("=============================================================");
            log.info("Public URL: {}", getPublicUrl());
            log.info("=============================================================");
            lastError = null;
        } catch (Exception e) {
            log.error("Failed to start ngrok: ", e);
            lastError = extractErrorMessage(e);
            closeNgrok();
        }
    }

    public String getLastError() {
        return lastError;
    }

    private String extractErrorMessage(Exception e) {
        String message = e.getMessage();
        if (StringUtils.isNotEmpty(message)) {
            if (message.contains("too old") || message.contains("ERR_NGROK")) {
                return "Ngrok agent needs updating. Run 'ngrok update' or download from https://ngrok.com/download";
            }
            if (message.contains("authentication failed")) {
                return "Ngrok authentication failed. Please verify your auth token.";
            }
        }
        return "Ngrok failed to start: " + StringUtils.defaultString(message, "Unknown error");
    }

    public Optional<String> getPublicUrl() {
        return Optional.ofNullable(tunnel)
                .map(Tunnel::getPublicUrl);
    }

    @PreDestroy
    public void closeNgrok() {
        try {
            if (ngrokClient != null) {
                if (tunnel != null) {
                    ngrokClient.disconnect(String.valueOf(tunnel));
                }
                ngrokClient.kill();
            }
        } catch (Exception e) {
            log.warn("Error closing ngrok: {}", e.getMessage());
        } finally {
            ngrokClient = null;
            tunnel = null;
        }
    }
}
