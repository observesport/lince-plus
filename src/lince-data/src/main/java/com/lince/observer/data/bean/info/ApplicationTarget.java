package com.lince.observer.data.bean.info;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Edition of the running application. Serialised in lowercase so the dashboard can
 * compare it with its own build target ("desktop" | "cloud").
 * <p>
 * Created by Alberto Soto. 23/9/26 (LO-343)
 */
public enum ApplicationTarget {
    DESKTOP("desktop"),
    CLOUD("cloud");

    private final String label;

    ApplicationTarget(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
