package com.lince.observer.data.bean.info;

/**
 * Minimal liveness answer for the info endpoint (LO-343).
 * If the server can build this object, it is up.
 *
 * @param status liveness status, {@link #STATUS_UP} when healthy
 * @param target edition answering the request
 * <p>
 * Created by Alberto Soto. 23/9/26
 */
public record ApplicationHealth(String status, ApplicationTarget target) {

    public static final String STATUS_UP = "UP";

    public static ApplicationHealth up(ApplicationTarget target) {
        return new ApplicationHealth(STATUS_UP, target);
    }
}
