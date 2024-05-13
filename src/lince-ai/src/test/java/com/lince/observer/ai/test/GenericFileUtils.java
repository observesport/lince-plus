package com.lince.observer.ai.test;

import java.io.*;

/**
 * com.lince.observer.data
 * Class GenericFileUtils
 * 12/06/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class GenericFileUtils {
    private static final String RESOURCE_PATH = "src/test/resources/";

    public static String getResourcePath(String file) {
        return RESOURCE_PATH + file;
    }

    public static InputStream getInputReader(String file) throws FileNotFoundException {
        InputStream input = new FileInputStream(new File(getResourcePath(file)));
        return input;
    }

    public static FileWriter getWriter(String file) throws IOException {
        return new FileWriter(getResourcePath(file));
    }
}
