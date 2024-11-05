package com.lince.observer.data.legacy.utiles;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.io.InputStreamReader;
import java.io.IOException;

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
                ResourceBundle resourceBundle = ResourceBundle.getBundle(bundle, Locale.getDefault(), new UTF8Control());
                return new String(resourceBundle.getString(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("I18n Message", e);
        }
        return key;
    }

    private static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStreamReader reader = new InputStreamReader(loader.getResourceAsStream(resourceName), StandardCharsets.UTF_8)) {
                return new PropertyResourceBundle(reader);
            }
        }
    }
}
