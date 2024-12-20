package com.lince.observer.data.base;

import javafx.stage.Stage;

/**
 * com.lince.observer.data
 * Class ILinceApp
 * 12/04/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public interface ILinceApp {
    public Stage getPrimaryStage();
    public String getWindowTitle();
    public DataHubServiceBase getDataHubService();


}
