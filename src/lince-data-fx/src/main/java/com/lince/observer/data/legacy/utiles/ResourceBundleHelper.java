package com.lince.observer.data.legacy.utiles;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by deicos on 08/06/2015.
 */
public class ResourceBundleHelper {
    private static final String CURRENT_BUNDLE = "i18n.Bundle";
    private static Logger log = LoggerFactory.getLogger(ResourceBundleHelper.class);
    /**
     * @param key
     * @return
     */
    public static String getI18NLabel(String key) {
        return getI18NLabel(CURRENT_BUNDLE, key);
    }

    /**
     * @param bundle
     * @param key
     * @return
     */
    public static String getI18NLabel(String bundle, String key) {
        try {
            if (StringUtils.isNotEmpty(bundle) && StringUtils.isNotEmpty(key)) {
                return java.util.ResourceBundle.getBundle(bundle).getString(key);
            }
        } catch (Exception e) {
            log.error("I18n Message",e);
        }
        return key;
    }
}
