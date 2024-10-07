package com.lince.observer.data.service;

import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.util.SystemNetworkHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Created by Alberto Soto. 1/11/22
 */
@Service
@DesktopQualifier
public class SystemService {
    protected final Environment environment;

    public SystemService(Environment environment) {
        this.environment = environment;
    }

    public String getCurrentServerURI(){
        String url = String.format("http://%s:%s", StringUtils.removeStart(SystemNetworkHelper.getMacAccessibleIp(), "/"), getCurrentPort());
        return url;
    }
    public Integer getCurrentPort() {
        String port = environment.getProperty("local.server.port");
        if (StringUtils.isNotEmpty(port)) {
            return Integer.valueOf(port);
        }
        return null;
    }
}
