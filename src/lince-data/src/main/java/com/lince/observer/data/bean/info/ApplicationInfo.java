package com.lince.observer.data.bean.info;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Public, unauthenticated description of the running application.
 * Shared contract between the desktop embedded server and the cloud API (LO-343).
 * Optional fields (build, buildTime) are omitted from the JSON when unknown so the
 * dashboard can simply test for presence.
 *
 * @param name      human readable application name
 * @param version   Maven project version, null when unknown
 * @param build     git sha or build identifier, null when unknown
 * @param buildTime ISO-8601 build timestamp, null when unknown
 * @param target    edition answering the request
 * <p>
 * Created by Alberto Soto. 23/9/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApplicationInfo(String name, String version, String build, String buildTime, ApplicationTarget target) {
}
