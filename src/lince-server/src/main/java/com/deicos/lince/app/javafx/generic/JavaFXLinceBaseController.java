package com.deicos.lince.app.javafx.generic;

import com.deicos.lince.app.LinceApp;
import com.deicos.lince.app.javafx.JavaFXLoader;
import com.deicos.lince.data.util.JavaFXLogHelper;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * lince-scientific-base
 * com.deicos.lince.app.javafx.generic
 * @author berto (alberto.soto@gmail.com)in 24/02/2017.
 * Description:
 */
public abstract class JavaFXLinceBaseController {


    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    // Reference to the main application.
    protected LinceApp mainLinceApp;

    public LinceApp getMainLinceApp() {
        return mainLinceApp;
    }

    public void setMainLinceApp(LinceApp mainLinceApp) {
        this.mainLinceApp = mainLinceApp;
    }

    @FXML
    private void handleAbout() {
        JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION
                , "About"
                , "\n Author: Alberto Soto Fernandez\nhttps://www.albertosoto.es");
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        JavaFXLoader.exit();
    }


    //para sobreescribir e iniciar posterior a cargas de construccion: RootLayoutController
    public void lazyInit() {
    }

    /**
     * Creates an standard dialog to open old swing content into javafx dialog
     *
     * @param component
     */
    protected void openEmbedded(JComponent component, String title) {
        final SwingNode swingNode = new SwingNode();
        swingNode.setContent(component);
        Dialog dlg = new Dialog();
        dlg.setHeaderText(StringUtils.defaultIfEmpty(title, "Legacy - Swing component"));
        StackPane pane1 = new StackPane();
        pane1.getChildren().add(swingNode);
        pane1.setMinSize(600, 400);
        dlg.getDialogPane().setContent(pane1);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        dlg.setResizable(true);
        dlg.show();
    }

}
