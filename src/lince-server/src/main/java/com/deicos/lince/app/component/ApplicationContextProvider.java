package com.deicos.lince.app.component;

import com.deicos.lince.app.ServerAppParams;
import com.deicos.lince.app.base.PropertyLoader;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
    private static JSONObject lastLinceVersion = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    /**
     * Reads all input into a string
     *
     * @param rd reader
     * @return string
     * @throws IOException any
     */
    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * Reads JSON file from URL
     *
     * @param url web url
     * @return JSON object
     * @throws IOException   e
     * @throws JSONException e
     */
    public JSONObject readJSONFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
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

    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");  // name of the resource bundle
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    public String getMessage(String label, Object... msgParameters) {
        return messageSource().getMessage(label, msgParameters, Locale.getDefault());
    }

    /**
     * Checks the last published version of Lince PlUS at public repo and returns the value
     * Safe and lazy singleton init to avoid extra RQ
     * @return JSON object
     */
    public JSONObject getLastLinceVersion() {
        try {
            if (lastLinceVersion == null) {
                //  Resource resource = getApplicationContext().getResource("url:" + ServerAppParams.LINCE_GIT_VERSION);
                lastLinceVersion = readJSONFromUrl(ServerAppParams.LINCE_GIT_VERSION);
                String currentVersion = getRunVersion();
                String lastVersion = lastLinceVersion.getString("version");
                boolean isUpdate = !StringUtils.equals(currentVersion, lastVersion);
                lastLinceVersion.put("update", isUpdate);
                if (isUpdate) {
                    lastLinceVersion.put("updateMsg"
                            , getMessage("lince.availableUpdate", lastLinceVersion.get("version")));
                }
            }
        } catch (Exception e) {
            lastLinceVersion = new JSONObject();
        }
        return lastLinceVersion;
    }

}