package com.lince.observer.desktop.spring.service;

import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.bean.info.ApplicationHealth;
import com.lince.observer.data.bean.info.ApplicationInfo;
import com.lince.observer.data.bean.info.ApplicationTarget;
import com.lince.observer.data.service.ApplicationInfoService;
import com.lince.observer.data.util.PropertyLoader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Desktop edition of {@link ApplicationInfoService} (LO-343).
 * <p>
 * Version resolution order:
 * <ol>
 *   <li>{@code app.version} from the Spring environment (Maven-filtered from the pom, the same key
 *       {@link PropertyLoader#getVersionNumber()} uses for the splash screen);</li>
 *   <li>{@link BuildProperties#getVersion()} when {@code META-INF/build-info.properties} exists;</li>
 *   <li>unknown (null), so the dashboard hides the version row.</li>
 * </ol>
 * The literal {@code @version@} placeholder is treated as unknown: it shows up when the classpath holds
 * an unfiltered copy of application.properties (IDE runs on target/classes).
 * <p>
 * Created by Alberto Soto. 23/9/26
 */
@Service
@DesktopQualifier
public class DesktopApplicationInfoService implements ApplicationInfoService {

    static final String UNFILTERED_VERSION_PLACEHOLDER = "@version@";
    static final String NAME_PROPERTY = "app.ui.title";
    static final String APPLICATION_NAME_PROPERTY = "spring.application.name";
    static final String DEFAULT_NAME = "Lince PLUS";

    private final Environment environment;
    private final ObjectProvider<BuildProperties> buildProperties;

    public DesktopApplicationInfoService(Environment environment, ObjectProvider<BuildProperties> buildProperties) {
        this.environment = environment;
        this.buildProperties = buildProperties;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        BuildProperties build = buildProperties.getIfAvailable();
        return new ApplicationInfo(resolveName(), resolveVersion(build), null, resolveBuildTime(build), ApplicationTarget.DESKTOP);
    }

    @Override
    public ApplicationHealth getHealth() {
        return ApplicationHealth.up(ApplicationTarget.DESKTOP);
    }

    private String resolveName() {
        String name = environment.getProperty(NAME_PROPERTY);
        if (StringUtils.isBlank(name)) {
            name = environment.getProperty(APPLICATION_NAME_PROPERTY);
        }
        return StringUtils.isBlank(name) ? DEFAULT_NAME : name.trim();
    }

    private String resolveVersion(BuildProperties build) {
        String version = environment.getProperty(PropertyLoader.VERSION_NUMBER);
        if (isKnownVersion(version)) {
            return version.trim();
        }
        if (build != null && isKnownVersion(build.getVersion())) {
            return build.getVersion().trim();
        }
        return null;
    }

    private String resolveBuildTime(BuildProperties build) {
        if (build == null) {
            return null;
        }
        Instant time = build.getTime();
        return time == null ? null : time.toString();
    }

    static boolean isKnownVersion(String version) {
        return StringUtils.isNotBlank(version) && !StringUtils.contains(version, UNFILTERED_VERSION_PLACEHOLDER);
    }
}
