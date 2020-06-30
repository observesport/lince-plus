package com.deicos.lince.app.javafx.components;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * com.deicos.lince.app.javafx.components
 * Class JavaFXBrowser
 * 10/06/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class JavaFXBrowser extends Region {

    private final WebView browser = new WebView();

    public JavaFXBrowser(String uri) {
        //apply the styles
        getStyleClass().add("browser");
        // load the web page
        final WebEngine webEngine = browser.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.load(uri);
        //add the web view to the scene
        getChildren().add(browser);

    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return getHeight();
    }

    @Override
    protected double computePrefHeight(double width) {
        return getWidth();
    }
}