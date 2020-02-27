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
package com.deicos.lince.app;


import com.deicos.lince.app.base.AbstractJavaFxApplicationSupport;
import com.deicos.lince.app.component.ApplicationContextProvider;
import com.deicos.lince.app.javafx.JavaFXLoader;
import com.deicos.lince.app.service.VideoService;
import com.deicos.lince.data.barcode.QRCodeGenerator;
import com.deicos.lince.data.system.operations.LinceFileHelper;
import com.deicos.lince.data.util.JavaFXLogHelper;
import com.deicos.lince.data.util.SystemNetworkHelper;
import com.deicos.lince.math.service.*;
import javafx.application.Preloader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

/**
 * Lince-desktop
 * com.deicos.lince.app.App
 * Created by Alberto Soto Fernandez in 21/01/2016.
 * Description:
 */

@SpringBootApplication
@Configuration
    @ComponentScan(basePackages = "com.deicos.lince")
//@EnableWebSecurity
@EnableScheduling
public class LinceApp extends AbstractJavaFxApplicationSupport {

    /**
     * Note that this is configured in application.properties
     */
    @Value("${app.ui.title:Lince App}")//
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
    protected DataHubService dataHubService;


    /**
     * Constructor
     */
    public LinceApp() {
    }

    public static void main(String[] args) {
        launchApp(LinceApp.class, args); // para salvar los parametros
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

    private File codeQR = null;


    @Override
    public void initRootLayout() {
        JavaFXLoader loader = new JavaFXLoader("javafx/view/RootLayout.fxml", this);
        loader.loadFXMLStage();
        rootLayout = (BorderPane) loader.getPane();
        // Try to load last opened person file.
        LinceFileHelper fileHelper = new LinceFileHelper();
        File file = fileHelper.getLinceProjectFilePath();
        if (file != null) {
            fileHelper.loadLinceProjectFromFile(file, this);
        }
        generateQRCode();
    }


    private void generateQRCode() {
        try {
            String url = String.format("http://%s:%s", StringUtils.removeStart(SystemNetworkHelper.getMacAccessibleIp(), "/"), getCurrentPort());
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
