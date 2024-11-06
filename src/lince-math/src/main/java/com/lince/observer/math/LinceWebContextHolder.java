package com.lince.observer.math;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.context.Theme;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.sql.DataSource;
import java.util.Locale;

/**
 * com.lince.observer.math
 * Class WebContextHolder
 *
 * @author berto (alberto.soto@gmail.com). 09/03/2019
 * @author 应卓(yingzhor @ gmail.com)
 * @see <a href="https://stackoverflow.com/questions/1629211/how-do-i-get-the-session-object-in-spring">How do I get the session object in Spring?</a>
 */
public class LinceWebContextHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinceWebContextHolder.class);

    private static final LinceWebContextHolder INSTANCE = new LinceWebContextHolder();

    public static LinceWebContextHolder get() {
        return INSTANCE;
    }

    private LinceWebContextHolder() {
        super();
    }

    // --------------------------------------------------------------------------------------------------------------

    public HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest();
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public HttpSession getSession(boolean create) {
        return getRequest().getSession(create);
    }

    public ServletContext getServletContext() {
        return getSession().getServletContext();    // servlet2.3
    }

    public Locale getLocale() {
        return RequestContextUtils.getLocale(getRequest());
    }

    public Theme getTheme() {
        return RequestContextUtils.getTheme(getRequest());
    }

    public ApplicationContext getApplicationContext() {
        return WebApplicationContextUtils.getWebApplicationContext(getServletContext());
    }

    public DataSource getDataSource() {
        return getBeanFromApplicationContext(DataSource.class);
    }

    private <T> T getBeanFromApplicationContext(Class<T> requiredType) {
        try {
            return getApplicationContext().getBean(requiredType);
        } catch (NoUniqueBeanDefinitionException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }

//    public String getSessionId() {
//        return getSession().getId();
//    }
//
//    public ApplicationEventPublisher getApplicationEventPublisher() {
//        return (ApplicationEventPublisher) getApplicationContext();
//    }
//
//    public LocaleResolver getLocaleResolver() {
//        return RequestContextUtils.getLocaleResolver(getRequest());
//    }
//
//    public ThemeResolver getThemeResolver() {
//        return RequestContextUtils.getThemeResolver(getRequest());
//    }
//
//    public ResourceLoader getResourceLoader() {
//        return (ResourceLoader) getApplicationContext();
//    }
//
//    public ResourcePatternResolver getResourcePatternResolver() {
//        return (ResourcePatternResolver) getApplicationContext();
//    }
//
//    public MessageSource getMessageSource() {
//        return (MessageSource) getApplicationContext();
//    }
//
//    public ConversionService getConversionService() {
//        return getBeanFromApplicationContext(ConversionService.class);
//    }
//
//    public Collection<String> getActiveProfiles() {
//        return Arrays.asList(getApplicationContext().getEnvironment().getActiveProfiles());
//    }
//
//    public ClassLoader getBeanClassLoader() {
//        return ClassUtils.getDefaultClassLoader();
//    }


}
