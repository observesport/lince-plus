package com.deicos.lince.app.javafx.view;

import com.deicos.lince.app.javafx.generic.JavaFXLinceBaseController;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

/**
 * lince-scientific-base
 * com.deicos.lince.app.javafx.view
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
