package com.deicos.lince.app;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * lince-scientific-base
 * com.deicos.lince.app
 * Created by Alberto Soto Fernandez in 10/07/2017.
 * Description:
 */
public class SpringFxmlLoader {

    private static final ApplicationContext applicationContext = new AnnotationConfigApplicationContext(LinceApp.class);

    public <T> T load(URL url) {
        try  {
            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(applicationContext::getBean);
            return loader.load();
        } catch (Exception ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public <T> T load(String url, String resources) {
        FXMLLoader loader = new FXMLLoader();
        //2loader.setControllerFactory(clazz -> applicationContext.getBean(clazz));
        loader.setControllerFactory(applicationContext::getBean);
        loader.setLocation(getClass().getResource(url));
        loader.setResources(ResourceBundle.getBundle(resources));
        try {
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}