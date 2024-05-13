package com.deicos.lince.app.component;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * com.deicos.lince.app.component
 * Class I18nMessageProvider
 * 09/12/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Component
public class I18nMessageProvider {
    private Locale currentLocale = new Locale("es", "ES");
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(currentLocale); // Set default Locale as US
        return slr;
    }
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");  // name of the resource bundle
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    public String getMessage(String label) {
        return getMessage(label, null);
    }

    public String getMessage(String label, Object... msgParameters) {
        return messageSource().getMessage(label, msgParameters, Locale.getDefault());
    }
}
