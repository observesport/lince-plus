package com.lince.observer.desktop.javafx.view;

import com.lince.observer.desktop.javafx.generic.JavaFXLinceBaseController;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

/**
 * lince-scientific-base
 * .app.javafx.view
 * @author berto (alberto.soto@gmail.com)in 28/02/2017.
 * Description:
 */
public class HomeController extends JavaFXLinceBaseController {

    @FXML
    private WebView webComponent;

    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        //webComponent.getEngine().load("https://www.oracle.com/products/index.html");
        log.info("--            Init Webcomponent               --");
    }


}
