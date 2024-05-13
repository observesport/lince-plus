package com.lince.observer.data.base;

import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

/**
 * com.lince.observer.ai
 * Class EmptyLinceApp
 * 12/04/2019
 * <p>
 * Class implementing LinceApp operations for testing purpose and programming
 * Not suitable for production use
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class EmptyLinceApp implements ILinceApp {

    @Override
    public Stage getPrimaryStage() {
        return null;
    }

    @Override
    public String getWindowTitle() {
        return StringUtils.EMPTY;
    }

    @Override
    public DataHubServiceBase getDataHubService() {
        return new DataHubServiceBase();
    }
}
