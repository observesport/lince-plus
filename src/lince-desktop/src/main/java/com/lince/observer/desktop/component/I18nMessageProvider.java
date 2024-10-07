package com.lince.observer.desktop.component;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import java.util.Locale;

/**
 * .app.component
 * Class I18nMessageProvider
 * 09/12/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Component
public class I18nMessageProvider {

    private final ResourceBundleMessageSource messageSource;

    public I18nMessageProvider(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String label) {
        return getMessage(label, null);
    }

    public String getMessage(String label, Object... msgParameters) {
        return messageSource.getMessage(label, msgParameters, Locale.getDefault());
    }
}
