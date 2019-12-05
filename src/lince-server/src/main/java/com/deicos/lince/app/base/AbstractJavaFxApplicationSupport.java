/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deicos.lince.app.base;

import com.deicos.lince.app.helper.ServerValuesHelper;
import com.deicos.lince.app.javafx.AppPreloader;
import com.deicos.lince.data.base.ILinceApp;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * @author Thomas Darimont
 */
public abstract class AbstractJavaFxApplicationSupport extends Application implements ILinceApp {

    private static String[] savedArgs;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private Locale currentLocale = new Locale("es", "ES");

    protected Stage primaryStage;

    public BorderPane getRootLayout() {
        return rootLayout;
    }

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    protected BorderPane rootLayout;
    protected ConfigurableApplicationContext applicationContext;


    public ApplicationContext getContext() {
        return context;
    }

    @Autowired
    protected ApplicationContext context;
    @Autowired
    protected Environment environment;


    @Override
    public void init() throws Exception {
        System.setProperty("java.awt.headless", "false"); //there was a headless error on swing parts (legacy components)
        applicationContext = SpringApplication.run(getClass(), savedArgs);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }

    public abstract void initRootLayout();

    public abstract String getWindowTitle();

    public abstract String getWindowIcon();

    public abstract void onStart(Stage primaryStage);

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(getWindowTitle());
        // Set the application icon.
        this.primaryStage.getIcons().add(new Image(getWindowIcon()));
        initRootLayout();
        onStart(primaryStage);
    }

    @Override
    public void stop() throws Exception {

        super.stop();
        applicationContext.close();
    }

    protected static void launchApp(Class<? extends AbstractJavaFxApplicationSupport> appClass, String[] args) {

        AbstractJavaFxApplicationSupport.savedArgs = args;
        //Application.launch(appClass, args);
        //SpringApplication.run(appClass, args); ==> esta en el init - ya es spring. Ver application context.
        LauncherImpl.launchApplication(appClass, AppPreloader.class, args);
    }

    public Integer getCurrentPort() {
        String port = environment.getProperty("local.server.port");
        if (StringUtils.isNotEmpty(port)) {
            return Integer.valueOf(port);
        }
        return null;
    }

    public String getServerURL() {
        return ServerValuesHelper.getServerURL(context, getCurrentPort());
    }

    /**
     * Dark area
     * **/


    /**
     * @return
     */
    @Bean
    public WebMvcConfigurer CORSConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**");
            }
        };
    }
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
        return getMessage(label,null);
    }

    public String getMessage(String label, Object... msgParameters) {
        return messageSource().getMessage(label, msgParameters, currentLocale);
    }
}
