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
package com.lince.observer.desktop.base;

import com.lince.observer.data.base.ILinceApp;
import com.lince.observer.desktop.component.I18nMessageProvider;
import com.lince.observer.desktop.helper.ServerValuesHelper;
import com.lince.observer.desktop.javafx.AppPreloader;
import com.lince.observer.data.service.SystemService;
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
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by Alberto Soto
 */
public abstract class AbstractJavaFxApplicationSupport extends Application implements ILinceApp {

    //    private static String[] savedArgs;
    protected final Logger log = LoggerFactory.getLogger(AbstractJavaFxApplicationSupport.class);
    protected Stage primaryStage;
    protected BorderPane rootLayout;


    public BorderPane getRootLayout() {
        return rootLayout;
    }

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    protected ConfigurableApplicationContext applicationContext;


    public abstract ApplicationContext getContext();

    public abstract Environment getEnvironment();

    public abstract SystemService getSystemService();

    public abstract I18nMessageProvider getI18nMessageProvider();
    //@Autowired
    //protected ApplicationContext context;
    //@Autowired
    //protected Environment environment;
    //@Autowired
    //protected SystemService systemService;
    //@Autowired
    //protected I18nMessageProvider i18nMessageProvider;



/*    @Override
    public void init() throws Exception {
        System.setProperty("java.awt.headless", "false"); //there was a headless error on swing parts (legacy components)
        applicationContext = SpringApplication.run(getClass(), savedArgs);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }*/

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

//        AbstractJavaFxApplicationSupport.savedArgs = args;
//        https://stackoverflow.com/questions/59656908/problem-with-javafx-intellij-setting-when-try-to-use-launcherimpl-for-preloader
        System.setProperty("javafx.preloader", AppPreloader.class.getName());
        //Application.launch(appClass, args);
        //SpringApplication.run(appClass, args); ==> esta en el init - ya es spring. Ver application context.
        LauncherImpl.launchApplication(appClass, AppPreloader.class, args);
    }

    public Integer getCurrentPort() {
        if (getSystemService() != null) {
            return getSystemService().getCurrentPort();
        } else {
            return null;
        }
    }

    public String getServerURL() {
        try {
            return ServerValuesHelper.getServerURL(getContext(), getCurrentPort());
        } catch (Exception e) {
            log.error("getting server URL", e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * Dark area
     * **/


    /**
     * @return
     */
    @Bean
    public WebMvcConfigurer CORSConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**");
            }
        };
    }

    public String getMessage(String label) {
        return getI18nMessageProvider().getMessage(label, (Object[]) null);
    }

    public String getMessage(String label, Object... msgParameters) {
        return getI18nMessageProvider().getMessage(label, msgParameters);
    }
}
