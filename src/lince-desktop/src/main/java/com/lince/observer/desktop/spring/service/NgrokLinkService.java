package com.lince.observer.desktop.spring.service;

import com.github.alexdlaird.ngrok.process.NgrokLog;
import com.lince.observer.data.LinceQualifier;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.desktop.component.NgrokConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Alberto Soto. 11/2/25
 */
@Service
@LinceQualifier.DesktopQualifier
public class NgrokLinkService implements ExternalLinkService {
    private final Map<String, String> configurationValues = new HashMap<>();
    private final NgrokConfig ngrokConfig;
    private static final String NGROK_TOKEN = "ngrok_token";

    final Function<NgrokLog, Void> logEventCallback = ngrokLog -> {
        log.info("External Link informationL:{}", ngrokLog.getLine());
        JavaFXLogHelper.addLogInfo(ngrokLog.getLine());
        return null;
    };

    public NgrokLinkService() {
        this.ngrokConfig = new NgrokConfig(logEventCallback);
    }

    @Override
    public Map<String, String> getConfigurationParameters() {
        Map<String, String> params = new HashMap<>();
        params.put(NGROK_TOKEN, "Ngrok Authentication Token");
        return params;
    }

    @Override
    public String generateLink(int port) {
        ngrokConfig.setToken(getParameter(NGROK_TOKEN));
        ngrokConfig.startNgrok(port);
        Optional<String> publicUrl = ngrokConfig.getPublicUrl();
        if (publicUrl.isPresent()) {
            log.info("Generated Ngrok link: {}", publicUrl);
        }
        return publicUrl.orElse(StringUtils.EMPTY);
    }

    @Override
    public void disconnect() {
        ngrokConfig.closeNgrok();
        log.info("Ngrok tunnel closed");
    }

    @Override
    public Map<String, String> getConfiguration() {
        Map<String, String> validKeys = getConfigurationParameters();
        return configurationValues.entrySet().stream()
                .filter(entry -> validKeys.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void setConfiguration(Map<String, String> parameters) {
        Map<String, String> validKeys = getConfigurationParameters();
        configurationValues.clear();
        parameters.entrySet().stream()
                .filter(entry -> validKeys.containsKey(entry.getKey()))
                .forEach(entry -> configurationValues.put(entry.getKey(), entry.getValue()));
    }

    @Override
    public void setParameter(String key, String value) {
        if (getConfigurationParameters().containsKey(key)) {
            configurationValues.put(key, value);
        }
    }

    @Override
    public String getParameter(String key) {
        if (getConfigurationParameters().containsKey(key)) {
            return configurationValues.get(key);
        }
        return null;
    }

    @Override
    public boolean isConnected() {
        return StringUtils.isNotEmpty(getExternalLink());
    }

    @Override
    public String getExternalLink() {
        return ngrokConfig.getPublicUrl().orElse(StringUtils.EMPTY);
    }

    @Override
    public String getConfigurationMessage() {
        return "To configure Ngrok, you need to obtain an authentication token.\n"
                + "Please follow these steps:\n\n"
                + "1. Go to https://dashboard.ngrok.com/get-started/your-authtoken\n"
                + "2. Sign in to your Ngrok account (or create one if you don't have it)\n"
                + "3. Copy your authentication token from the dashboard\n"
                + "4. Paste the token in the 'Ngrok Authentication Token' field in the configuration settings";
    }

    public void clearConfiguration() {
        configurationValues.clear();
    }
}
