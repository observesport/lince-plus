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
import com.deicos.lince.app.javafx.JavaFXLoader;
import com.deicos.lince.app.javafx.view.example.BirthdayStatisticsController;
import com.deicos.lince.app.javafx.view.example.PersonEditDialogController;
import com.deicos.lince.app.service.VideoService;
import com.deicos.lince.data.bean.example.Person;
import com.deicos.lince.data.system.operations.LinceFileHelper;
import com.deicos.lince.data.util.JavaFXLogHelper;
import com.deicos.lince.math.service.*;
import javafx.application.Preloader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * Lince-desktop
 * com.deicos.lince.app.App
 * Created by Alberto Soto Fernandez in 21/01/2016.
 * Description:
 */

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.deicos.lince")
public class LinceApp extends AbstractJavaFxApplicationSupport {

    /**
     * Note that this is configured in application.properties
     */
    @Value("${app.ui.title:Lince App}")//
    public String windowTitle;
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


    @Override
    public void initRootLayout() {
        JavaFXLoader loader = new JavaFXLoader("javafx/view/RootLayout.fxml", this);
        loader.loadFXMLStage();
        rootLayout = (BorderPane) loader.getPane();
        // Try to load last opened person file.
        // LinceFileHelper fileHelper = new LinceFileHelper(categoryService.getCriteria(), analysisService.getCurrentDataRegister());
        LinceFileHelper fileHelper = new LinceFileHelper();
        File file = fileHelper.getLinceProjectFilePath();
        if (file != null) {
            fileHelper.loadLinceProjectFromFile(file, this);
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
        log.info("==============================================================================");
        log.info("   Remote uri     :" + url);
        log.info("==============================================================================");
        JavaFXLogHelper.addLogInfo("Arrancado lince server en la URL " + url);
        String port = environment.getProperty("local.server.port");
        if (StringUtils.isNotEmpty(port)) {
            JavaFXLogHelper.addLogInfo("Puerto:" + port);
        }
    }

    /**
     * Opens a dialog to edit details for the specified person. If the user
     * clicks OK, the changes are saved into the provided person object and true
     * is returned.
     *
     * @param person the person object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showPersonEditDialog(Person person) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            JavaFXLoader fxLoader = new JavaFXLoader
                    <PersonEditDialogController>("javafx/view/example/PersonEditDialog.fxml", this);
            // Create the dialog Stage & Set the dialog icon.
            Stage dialogStage = fxLoader.getDialog("Edit Person", "file:resources/images/edit.png");
            // Set the person into the controller.
            PersonEditDialogController controller = (PersonEditDialogController) fxLoader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().getName(), e);
            return false;
        }
    }

    /**
     * Opens a dialog to show birthday statistics.
     */
    public void showBirthdayStatistics() {
        try {
            // Load the fxml file and create a new stage for the popup.
            JavaFXLoader fxLoader = new JavaFXLoader
                    <BirthdayStatisticsController>("javafx/view/example/BirthdayStatistics.fxml", this);
            Stage dialogStage = fxLoader.getDialog("Birthday Statistics", "file:resources/images/calendar.png");
            // Set the persons into the controller.
            BirthdayStatisticsController controller = (BirthdayStatisticsController) fxLoader.getController();
            controller.setPersonData(dataHubService.getUserData());
            // Set the dialog icon.
            dialogStage.show();
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().getName(), e);
        }
    }

}
