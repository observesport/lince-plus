package com.deicos.lince.app.component;

import com.deicos.lince.app.ServerAppParams;
import com.deicos.lince.app.base.PropertyLoader;
import com.deicos.lince.app.helper.ServerValuesHelper;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * com.deicos.lince.app.component
 * Class ApplicationContextProvider
 * 04/12/2019
 *
 * @author berto (alberto.soto@gmail.com)
 * <p>
 * Provdes application context. Use:
 * <p>
 * Autowired
 * ApplicationContextProvider applicationContextProvider;
 * <p>
 * ...
 * Message message = applicationContextProvider.getApplicationContext().getBean(Message.class);
 * ...
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final I18nMessageProvider messageProvider;
    private static JSONObject lastLinceVersion = new JSONObject();
    private static JSONObject userInformation = null;

    public ApplicationContextProvider(I18nMessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    /**
     * Returns current project version
     *
     * @return project-version
     */
    public String getRunVersion() {
        try {
            PropertyLoader propertyLoader = PropertyLoader.getInstance();
            return propertyLoader.getVersionNumber();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Reads the git version file and updates values into local static jsonobject
     */
    public void setLastLinceVersion() {
        try {
            lastLinceVersion = ServerValuesHelper.readJSONFromUrl(ServerAppParams.LINCE_GIT_VERSION);
            String currentVersion = getRunVersion();
            String lastVersion = lastLinceVersion.getString("version");
            boolean isUpdate = !StringUtils.equals(currentVersion, lastVersion);
            lastLinceVersion.put("update", isUpdate);
            if (isUpdate) {
                lastLinceVersion.put("updateMsg"
                        , messageProvider.getMessage("lince.availableUpdate", lastLinceVersion.get("version")));
            }
        } catch (Exception e) {
            lastLinceVersion = new JSONObject();
        }
    }

    /**
     * Checks the last published version of Lince PlUS at public repo and returns the value
     *
     * @return JSON object
     */
    public JSONObject getLastLinceVersion() {
        return lastLinceVersion;
    }


    public JSONObject getUserInformation(){
        try {
            userInformation = ServerValuesHelper.readJSONFromUrl(ServerAppParams.LINCE_USER_INFORMATION_PANEL);
        } catch (Exception e) {
            userInformation = new JSONObject();
        }
        return userInformation;
    }
}
