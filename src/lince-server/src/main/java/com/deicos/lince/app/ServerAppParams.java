package com.deicos.lince.app;

import com.deicos.lince.math.AppParams;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.rest.config
 *
 * @author berto (alberto.soto@gmail.com)in 21/01/2016.
 * Description:
 */
public class ServerAppParams extends AppParams {
    public static final String BASE_PACKAGE = "com.deicos.lince.app";
    public static final String VIDEO_TEST_ALIAS = "test";
    public static final String VIDEO_TEST_TYPE = "video/mp4";
    public static final String BASE_URL_STREAMING = "/getVideo/";
    public static final String CONFIGURATION_FILE = "application.properties";
    public static final String PARAM_LINCE_VERSION = "app.version";
    public static final String PARAM_PORT = "local.server.port";

    public static final String LINCE_GIT_VERSION = "https://raw.githubusercontent.com/observesport/lince-plus/master/lince-version.json";
    public static final long SCHEDULE_AUTOSAVE_WINDOW = 120000; //autosave cada 2 min
    public static final long SCHEDULE_UPDATE_CHECK_WINDOW = 1200000; //update check cada 20 min

}
