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
package com.lince.observer.desktop;


import com.github.alexdlaird.ngrok.NgrokClient;
import com.lince.observer.data.base.ILinceApp;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.data.service.ProfileService;
import com.lince.observer.data.util.QRCodeGenerator;
import com.lince.observer.desktop.component.ApplicationContextProvider;
import com.lince.observer.desktop.component.I18nMessageProvider;
import com.lince.observer.desktop.helper.ServerValuesHelper;
import com.lince.observer.desktop.javafx.AppPreloader;
import com.lince.observer.desktop.javafx.JavaFXLoader;
import com.lince.observer.data.service.SystemService;
import com.lince.observer.desktop.javafx.generic.JavaFXLinceBaseController;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.data.util.SystemNetworkHelper;
import com.lince.observer.desktop.spring.service.ExternalLinkService;
import com.lince.observer.math.service.*;
import com.lince.observer.transcoding.TranscodingProvider;
import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Lince-desktop
 * .app.App
 * Created by Alberto Soto Fernandez in 21/01/2016.
 * Description:
 */

@SpringBootApplication
@Configuration
@ComponentScan(basePackages = "com.lince.observer")
@EnableScheduling
public class LinceApp extends Application implements ILinceApp {

    private static final String WINDOW_ICON_PATH = "file:resources/images/address_book_32.png";
    private static ApplicationContext applicationContext;

    @Value("${app.ui.title:Lince App}")
    public String windowTitle;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected Stage primaryStage;
    protected BorderPane rootLayout;

    @Autowired
    ApplicationContextProvider applicationContextProvider;
    @Autowired
    LegacyConverterService legacyConverterService;
    @Autowired
    protected CategoryService categoryService;
    @Autowired
    protected AnalysisService analysisService;
    @Autowired
    protected ProfileService profileService;
    @Autowired
    protected SystemService systemService;
    @Autowired
    protected DataHubService dataHubService;
    @Autowired
    protected TranscodingProvider transcodingProvider;
    @Autowired
    protected ApplicationContext context;
    @Autowired
    protected Environment environment;
    @Autowired
    protected I18nMessageProvider i18nMessageProvider;
    @Autowired
    protected ExternalLinkService externalLinkService;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(LinceApp.class, args);
        //        https://stackoverflow.com/questions/59656908/problem-with-javafx-intellij-setting-when-try-to-use-launcherimpl-for-preloader
        System.setProperty("javafx.preloader", AppPreloader.class.getName());
        LauncherImpl.launchApplication(LinceApp.class, AppPreloader.class, args);
    }

    @Override
    public void init() throws Exception {
        System.setProperty("java.awt.headless", "false"); //there was a headless error on swing parts (legacy components)
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(getWindowTitle());
        this.primaryStage.getIcons().add(new Image(getWindowIcon()));
        initRootLayout();
        onStart(primaryStage);
    }

    @Override
    public String getWindowTitle() {
        return windowTitle;
    }


    /**
     * Configures CORS for the application.
     * @return WebMvcConfigurer with CORS configuration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**");
            }
        };
    }

    public ExternalLinkService getExternalLinkService() {
        return externalLinkService;
    }

    public LegacyConverterService getLegacyConverterService() {
        return legacyConverterService;
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public AnalysisService getAnalysisService() {
        return analysisService;
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public DataHubService getDataHubService() {
        return dataHubService;
    }

    public TranscodingProvider getTranscodingProvider() {
        return transcodingProvider;
    }

    public void initRootLayout() {
        JavaFXLoader<JavaFXLinceBaseController> loader = new JavaFXLoader<>("javafx/view/RootLayout.fxml", this);
        loader.loadFXMLStage();
        rootLayout = (BorderPane) loader.getPane();
        LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
        File file = fileHelper.getLinceProjectFilePath();
        if (file != null) {
            fileHelper.loadLinceProjectFromFile(file, this);
        }
        generateQRCode();
    }


    private void generateQRCode() {
        try {
            String url = systemService.getCurrentServerURI();
            QRCodeGenerator.generateQRCodeFileImage(url);
            log.info("QR Code to {}", url);
        } catch (Exception e) {
            log.error("Generating QR Code", e);
        }
    }

    public String getWindowIcon() {
        return WINDOW_ICON_PATH;
    }

    public void onStart(Stage primaryStage) {
        this.primaryStage = primaryStage;
        notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
        String url = getServerURL();
        String version = applicationContextProvider.getRunVersion();
        if (StringUtils.isNotEmpty(version)) {
            JavaFXLogHelper.addLogInfo("Running Lince PLUS version " + version);
        }
        log.info("==============================================================================");
        log.info("   Remote uri     :{}", url);
        log.info("==============================================================================");
        String ip = SystemNetworkHelper.getMacAccessibleIp();
        if (StringUtils.isNotEmpty(ip)) {
            JavaFXLogHelper.addLogInfo("Tu IP accesible es " + ip);
        }
        JavaFXLogHelper.addLogInfo("Puerto: " + getCurrentPort());
        JavaFXLogHelper.addLogInfo("Arrancado lince server en la URL " + url);
    }

    public BorderPane getRootLayout() {
        return rootLayout;
    }

    public void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Integer getCurrentPort() {
        return systemService.getCurrentPort();
    }

    public String getServerURL() {
        return ServerValuesHelper.getServerURL(context, getCurrentPort());
    }

    public String getMessage(String label) {
        return i18nMessageProvider.getMessage(label, (Object[]) null);
    }

    public String getMessage(String label, Object... msgParameters) {
        return i18nMessageProvider.getMessage(label, msgParameters);
    }
}
