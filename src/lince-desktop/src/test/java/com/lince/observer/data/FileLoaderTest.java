package com.lince.observer.data;


import com.lince.observer.data.base.ILinceApp;
import com.lince.observer.data.base.LinceFileHelperFx;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.export.Lince2ThemeExport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * com.lince.observer.ai.test
 * Class FileLoaderTest
 * 12/04/2019
 *
 * @author berto (alberto.soto@gmail.com)
 *
 */
class FileLoaderTest {
    private static LinceFileHelperFx fileLoader = null;
    private static ILinceApp linceApp;
    private static final Logger log = LoggerFactory.getLogger(FileLoaderTest.class);

    @BeforeAll
    static void init() {
        fileLoader = new LinceFileHelperFx();
        linceApp = new com.lince.observer.test.EmptyLinceApp();
    }
/*
    @AfterAll
    static void end() {
        BaseTest.showFinishMessage(NAME);
    }*/

    @Test
    void loadFileTest() {
        try {
            File file = new File(getClass().getResource("/multipleObserverExample.xml").getFile());
            fileLoader.loadLinceProjectFromFile(file, linceApp);
            Assertions.assertEquals(8, linceApp.getDataHubService().getDataRegister().get(0).getRegisterData().size());
        } catch (Exception e) {
            log.error("mek", e);
        }
//        BaseTest.showMessage("Fichero cargado");
    }

    /**
     *
     * A tiny tab-delimited Theme T-data file example:
     Time event
     5 :
     15 sue,b,run
     17 jack,e,talk
     201 sue,e,run
     231 bill,b,smile
     302 jack,b,talk
     302 bill,e,smile
     302 sue,b,smile
     303 &
     */
    @Test
    void transCodingRegisterToTheme() throws IOException {
        loadFileTest();
        LinceRegisterWrapper wrapper = linceApp.getDataHubService().getDataRegister().get(0);
        Assertions.assertNotNull(wrapper);
        Lince2ThemeExport export = new Lince2ThemeExport(wrapper.getRegisterData());
        export.createFile(GenericFileUtils.getWriter("transCodingRegisterToTheme2.vvt"));
    }


    @Test
    void testRandom(){
        int random = (int)(Math.random() * 1000 + 1);
        System.out.println(random);
    }

}
