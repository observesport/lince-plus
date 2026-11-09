package com.lince.observer.desktop.javafx.generic;

import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.desktop.LinceApp;
import com.lince.observer.desktop.javafx.JavaFXLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * lince-scientific-base
 * .app.javafx.generic
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
        JavaFXLoader.exit(getMainLinceApp());
    }


    //para sobreescribir e iniciar posterior a cargas de construccion: RootLayoutController
    public void lazyInit() {
    }

}
