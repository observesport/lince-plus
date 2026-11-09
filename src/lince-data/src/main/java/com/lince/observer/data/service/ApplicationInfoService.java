package com.lince.observer.data.service;

import com.lince.observer.data.bean.info.ApplicationHealth;
import com.lince.observer.data.bean.info.ApplicationInfo;

/**
 * Edition-specific source of the application version and health (LO-343).
 * <p>
 * One implementation per edition, following the {@code IHelloWorldService} pattern:
 * <ul>
 *   <li>desktop: {@code DesktopApplicationInfoService} in lince-desktop, tagged with
 *       {@link com.lince.observer.data.LinceQualifier.DesktopQualifier}, reads the Maven-filtered
 *       {@code app.version} property;</li>
 *   <li>cloud: implemented in lince-server, tagged with
 *       {@link com.lince.observer.data.LinceQualifier.CloudQualifier}, reads Spring Boot
 *       {@code BuildProperties} produced by the {@code build-info} goal.</li>
 * </ul>
 * <p>
 * Created by Alberto Soto. 23/9/26
 */
public interface ApplicationInfoService {

    /**
     * @return name, version and build metadata of the running application. Unknown values are null.
     */
    ApplicationInfo getApplicationInfo();

    /**
     * @return liveness status. Implementations should not touch session state or the UI.
     */
    ApplicationHealth getHealth();
}
