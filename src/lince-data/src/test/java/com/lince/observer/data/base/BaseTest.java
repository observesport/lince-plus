package com.lince.observer.data.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * com.lince.observer.ai.test.base
 * Class BaseTest
 * 12/04/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public abstract class BaseTest {
    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    public abstract String getTestName();

    public static void showInitMessage(String testName){
        log.info(String.format(" - Init test %s- ",testName));
    }
    public static void showFinishMessage(String testName){
        log.info(String.format(" - Fin test %s- ",testName));
    }

    public static void showMessage(String msg){
        log.info(msg);
    }

}
