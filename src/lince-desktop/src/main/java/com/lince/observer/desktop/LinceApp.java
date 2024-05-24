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


import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.data.service.ProfileService;
import com.lince.observer.desktop.base.AbstractJavaFxApplicationSupport;
import com.lince.observer.desktop.component.ApplicationContextProvider;
import com.lince.observer.desktop.component.SpringComponentScan;
import com.lince.observer.desktop.javafx.JavaFXLoader;
import com.lince.observer.data.service.SystemService;
import com.lince.observer.desktop.spring.service.VideoService;
import com.lince.observer.data.barcode.QRCodeGenerator;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.data.util.SystemNetworkHelper;
import com.lince.observer.math.service.*;
import com.lince.observer.transcoding.TranscodingProvider;
import javafx.application.Preloader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

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
public class LinceApp extends AbstractJavaFxApplicationSupport {

    /**
     * Note that this is configured in application.properties
     */
    @Value("${app.ui.title:Lince App}")
    public String windowTitle;
    @Autowired
    ApplicationContextProvider applicationContextProvider;
    @Autowired
    Environment environment;
    @Autowired
    LegacyConverterService legacyConverterService;
    @Autowired
    protected CategoryService categoryService;
    @Autowired
    protected AnalysisService analysisService;
    @Autowired
    protected ProfileService profileService;
    @Autowired
    protected VideoService videoService;
    @Autowired
    protected SystemService systemService;
    @Autowired
    protected DataHubService dataHubService;
    @Autowired
    protected TranscodingProvider transcodingProvider;
    private static ApplicationContext applicationContext;
    public static void main(String[] args) {
        applicationContext = SpringApplication.run(SpringComponentScan.class, args);
        checkBeansPresence("exampleBean", "springComponentScan");
        launchApp(LinceApp.class, args);
    }
    private static void checkBeansPresence(String... beans) {
        for (String beanName : beans) {
            System.out.println("Is " + beanName + " in ApplicationContext: " +
                    applicationContext.containsBean(beanName));
        }
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

    private File codeQR = null;


    @Override
    public void initRootLayout() {
        JavaFXLoader loader = new JavaFXLoader("javafx/view/RootLayout.fxml", this);
        loader.loadFXMLStage();
        rootLayout = (BorderPane) loader.getPane();
        // Try to load last opened person file.
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
            //getServerURL()
            codeQR = QRCodeGenerator.generateQRCodeFileImage(url);
            log.info("QR Code to " + url);
        } catch (Exception e) {
            log.error("Generating QR Code", e);
        }
    }


    @Override
    public String getWindowTitle() {
        return windowTitle;
    }

    @Override
    public String getWindowIcon() {
        return "file:resources/images/address_book_32.png";
    }

    @Override
    public void onStart(Stage primaryStage) {
        //cerramos el preloader
        notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
        String url = getServerURL();
        String version = applicationContextProvider.getRunVersion();
        if (StringUtils.isNotEmpty(version)) {
            JavaFXLogHelper.addLogInfo("Running Lince PLUS version " + version);
        }
        log.info("==============================================================================");
        log.info("   Remote uri     :" + url);
        log.info("==============================================================================");
        String ip = SystemNetworkHelper.getMacAccessibleIp();
        if (StringUtils.isNotEmpty(ip)) {
            JavaFXLogHelper.addLogInfo("Tu IP accesible es " + ip);
        }
        JavaFXLogHelper.addLogInfo("Puerto: " + getCurrentPort());
        JavaFXLogHelper.addLogInfo("Arrancado lince server en la URL " + url);
    }


}
