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
package com.lince.observer.desktop.javafx;

import com.lince.observer.data.util.PropertyLoader;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Tommy Ziegler
 * @author Thomas Darimont
 */
public class AppPreloader extends Preloader {

    private Stage stage;

//    private final I18nMessageProvider messageProvider;
//
//    public AppPreloader(I18nMessageProvider messageProvider) {
//        this.messageProvider = messageProvider;
//    }

    public void start(Stage stage) throws Exception {
        this.stage = stage;
        final VBox rootGroup = new VBox();

        final Label label = new Label();
        PropertyLoader propertyLoader = PropertyLoader.getInstance();
        label.setText("Lince PLUS Software for observation");
        label.setFont(new Font("Arial", 20));
        label.setTextFill(Color.WHITE);

        //final ProgressIndicator progressCircle = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        final Text description = new Text(100, 425, "Loading services...");
//        final Text description = new Text(100, 425, messageProvider.getMessage("loading_services"));
        description.setFill(Color.WHITE);

        final Text versionNumber = new Text(100, 475,propertyLoader.getVersionNumber());
        versionNumber.setFill(Color.WHITE);

        final HBox hb = new HBox();
        hb.setSpacing(100);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(label);


        final HBox hb3 = new HBox();
        hb3.setSpacing(5);
        hb3.setAlignment(Pos.CENTER);
        hb3.getChildren().addAll(description,versionNumber);

        //rootGroup.getChildren().addAll(label, progressCircle, description);
        final ProgressBar progressCircle = new ProgressBar(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressCircle.setStyle("-fx-accent: white;");
        final HBox progressBarHBox = new HBox();
        progressBarHBox.setSpacing(5);
        progressBarHBox.setAlignment(Pos.CENTER);
        progressBarHBox.getChildren().add(progressCircle);


        rootGroup.getChildren().addAll(hb, hb3);//progressBarHBox
        BackgroundImage myBI = new BackgroundImage(new Image("images/lince-splash.jpg", 616, 395, true, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT);
        rootGroup.setBackground(new Background(myBI));

        Scene scene = new Scene(rootGroup, 616, 395);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification pn) {
        if (pn instanceof StateChangeNotification) {
            stage.hide();
        }
    }
}
