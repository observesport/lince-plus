package com.lince.observer.desktop.provider;

import com.lince.observer.data.base.ILinceApp;
import com.lince.observer.data.base.ILinceAppProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

/**
 * Service provider implementation for ILinceApp
 * Uses Spring ApplicationContext to retrieve the LinceApp instance
 *
 * This class serves dual purposes:
 * 1. Spring Component - initialized by Spring and registers itself
 * 2. ServiceLoader provider - found by ServiceLoader when called from non-Spring contexts
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Component
public class LinceAppProvider implements ILinceAppProvider, ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static LinceAppProvider instance;

    public LinceAppProvider() {
        // Register this instance when created by Spring
        instance = this;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    @Override
    public ILinceApp getLinceApp() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext not initialized. Make sure Spring has fully started.");
        }
        return applicationContext.getBean(ILinceApp.class);
    }
}