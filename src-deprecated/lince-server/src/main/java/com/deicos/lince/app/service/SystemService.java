package com.deicos.lince.app.service;

import com.deicos.lince.data.util.SystemNetworkHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Created by Alberto Soto. 1/11/22
 */
@Service
public class SystemService {
    @Autowired
    protected Environment environment;
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
