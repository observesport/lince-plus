package com.lince.observer.data.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Class SpringMvcConfig
 * 31/05/2019
 *
 * Adds i18n support on Freemarker in a filter with custom language English
 *
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {
    @Autowired
    Environment environment;
    private final Locale currentLocale = new Locale("es", "ES");

//    @Bean
//    public LocaleResolver localeResolver() {
//        SessionLocaleResolver slr = new SessionLocaleResolver();
//        slr.setDefaultLocale(Locale.ENGLISH);
//        return slr;
//    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");  // name of the resource bundle
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(currentLocale); // Set default Locale as US
        return slr;
    }


    /*
   HTTPS AREA
    */
   /* @Bean
    public ServletWebServerFactory servletContainer() {
        //Enable SSL Trafic
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        // HTTP2HTTPS redirect
        tomcat.addAdditionalTomcatConnectors(httpToHttpsRedirectConnector());
        return tomcat;
    }

    private Connector httpToHttpsRedirectConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(getCurrentPort());
        connector.setSecure(true);
        connector.setRedirectPort(8443);
        return connector;
    }

    private Integer getCurrentPort() {
        String port = environment.getProperty("local.server.port");
        if (StringUtils.isNotEmpty(port)) {
            return Integer.valueOf(port);
        }
        return null;
    }
*/
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
