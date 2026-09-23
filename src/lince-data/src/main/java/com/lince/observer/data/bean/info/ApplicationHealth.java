package com.lince.observer.data.bean.info;

import java.util.Objects;

/**
 * Minimal liveness answer for the info endpoint (LO-343).
 * If the server can build this object, it is up.
 * <p>
 * Created by Alberto Soto. 23/9/26
 */
public class ApplicationHealth {

    public static final String STATUS_UP = "UP";

    private String status;
    private ApplicationTarget target;

    public ApplicationHealth() {
    }

    public ApplicationHealth(String status, ApplicationTarget target) {
        this.status = status;
        this.target = target;
    }

    public static ApplicationHealth up(ApplicationTarget target) {
        return new ApplicationHealth(STATUS_UP, target);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ApplicationTarget getTarget() {
        return target;
    }

    public void setTarget(ApplicationTarget target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationHealth)) return false;
        ApplicationHealth that = (ApplicationHealth) o;
        return Objects.equals(status, that.status) && target == that.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, target);
    }

    @Override
    public String toString() {
        return "ApplicationHealth{status='" + status + "', target=" + target + '}';
    }
}
