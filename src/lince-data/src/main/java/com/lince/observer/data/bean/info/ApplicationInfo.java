package com.lince.observer.data.bean.info;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Public, unauthenticated description of the running application.
 * Shared contract between the desktop embedded server and the cloud API (LO-343).
 * Optional fields (build, buildTime) are omitted from the JSON when unknown so the
 * dashboard can simply test for presence.
 * <p>
 * Created by Alberto Soto. 23/9/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationInfo {

    private String name;
    private String version;
    private String build;
    private String buildTime;
    private ApplicationTarget target;

    public ApplicationInfo() {
    }

    public ApplicationInfo(String name, String version, String build, String buildTime, ApplicationTarget target) {
        this.name = name;
        this.version = version;
        this.build = build;
        this.buildTime = buildTime;
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
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
        if (!(o instanceof ApplicationInfo)) return false;
        ApplicationInfo that = (ApplicationInfo) o;
        return Objects.equals(name, that.name)
                && Objects.equals(version, that.version)
                && Objects.equals(build, that.build)
                && Objects.equals(buildTime, that.buildTime)
                && target == that.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, build, buildTime, target);
    }

    @Override
    public String toString() {
        return "ApplicationInfo{name='" + name + "', version='" + version + "', build='" + build
                + "', buildTime='" + buildTime + "', target=" + target + '}';
    }
}
