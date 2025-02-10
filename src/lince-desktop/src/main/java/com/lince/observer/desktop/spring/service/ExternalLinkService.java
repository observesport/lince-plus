package com.lince.observer.desktop.spring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Alberto Soto. 11/2/25
 *
 */
public interface ExternalLinkService {
    Logger log = LoggerFactory.getLogger(ExternalLinkService.class);

    Map<String, String> parameters = new ConcurrentHashMap<>();

    /**
     * Generates an external link.
     *
     * @param port The port to use for the link.
     * @return The generated external link.
     */
    String generateLink(int port);


    Map<String, String> getConfigurationParameters();

    /**
     * Configures the external link service with the given parameters.
     *
     * @param parameters A map of configuration parameters.
     */
    default void configureService(Map<String, String> parameters) {
        Set<String> allowedKeys = getConfigurationParameters().keySet();
        Map<String, String> filteredParameters = parameters.entrySet().stream()
                .filter(entry -> allowedKeys.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        ExternalLinkService.parameters.putAll(filteredParameters);
        log.info("External link service configured with {} parameters", filteredParameters.size());

        if (filteredParameters.size() < parameters.size()) {
            log.warn("{} parameters were ignored as they are not in the allowed configuration keys",
                    parameters.size() - filteredParameters.size());
        }
    }

    /**
     * Disconnects the external link service.
     */
    void disconnect();

    /**
     * Sets a configuration parameter.
     *
     * @param key The parameter key.
     * @param value The parameter value.
     */
    default void setParameter(String key, String value) {
        parameters.put(key, value);
        log.debug("Parameter set: {} = {}", key, value);
    }

    /**
     * Gets a configuration parameter.
     *
     * @param key The parameter key.
     * @return The parameter value, or null if not found.
     */
    default String getParameter(String key) {
        String value = parameters.get(key);
        log.debug("Parameter get: {} = {}", key, value);
        return value;
    }

    /**
     * Checks if the service is currently connected.
     *
     * @return true if connected, false otherwise.
     */
    boolean isConnected();

    /**
     *
     * @return The generated external link.
     */
    String getExternalLink();

    /**
     *
     * @return A message indicating the current configuration needs.
     */
    String getConfigurationMessage();
}
