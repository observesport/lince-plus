package com.lince.observer.data.legacy.utiles;

import com.lince.observer.data.util.UTF8Control;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

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
        if (StringUtils.isEmpty(bundle) || StringUtils.isEmpty(key)) {
            return key;
        }

        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, Locale.getDefault(), new UTF8Control());
            if (resourceBundle.containsKey(key)) {
                return new String(resourceBundle.getString(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Error retrieving I18n message for key: {}", key, e);
        }

        return key;
    }
}
