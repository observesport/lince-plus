package com.deicos.lince.app.base;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * com.deicos.lince.app.base
 * Class PropertyLoader
 * 11/06/2019
 *
 * Property loader for contexts where Spring IoC is not available, like in javafx
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class PropertyLoader {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String RESOURCE_FILE = "/application.properties";
    private Properties prop;

    private static PropertyLoader instance = new PropertyLoader();
    public static final String VERSION_NUMBER = "app.version";


    private PropertyLoader() {
        try {
            this.prop = new Properties();
            prop.load(this.getClass().getResourceAsStream(RESOURCE_FILE));
        } catch (Exception e) {
            log.error("loading resource file", e);
        }
    }

    public static PropertyLoader getInstance() {
        return instance;
    }

    public String getValue(String key) {
        try {
            return prop.getProperty(key);
        } catch (Exception e) {
            log.error("getting value", e);
            return StringUtils.EMPTY;
        }
    }

    public String getVersionNumber(){
        return getValue(VERSION_NUMBER);
    }

}
