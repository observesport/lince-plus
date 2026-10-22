package com.lince.observer.data.bean.transfer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * What part of an imported file is merged into the loaded project (LO-349).
 * Serialised in lowercase ({@code "full"} | {@code "observations"}). Only the {@code lince-plus}
 * import type distinguishes both; the other importers always behave as {@link #FULL}.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public enum ImportScope {
    /**
     * Instrument, observations and research profiles (today's "Import Lince PLUS project"). Videos are not imported.
     */
    FULL("full"),
    /**
     * Observations only, matched against the loaded instrument (today's "Import observer").
     */
    OBSERVATIONS("observations");

    private final String label;

    ImportScope(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    /**
     * Parses the value sent by the client, ignoring case. Blank means {@link #FULL}.
     * Usable from a {@code @RequestParam String scope} without registering a Spring converter.
     *
     * @param value {@code full}, {@code observations} or blank
     * @return the scope
     * @throws IllegalArgumentException when the value is not a known scope
     */
    @JsonCreator
    public static ImportScope fromValue(String value) {
        if (StringUtils.isBlank(value)) {
            return FULL;
        }
        for (ImportScope scope : values()) {
            if (scope.label.equalsIgnoreCase(value.trim())) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Unknown import scope: " + value);
    }
}
