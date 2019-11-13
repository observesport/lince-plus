package com.deicos.lince.data;

import com.deicos.lince.data.barcode.QRCodeGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * com.deicos.lince.data
 * Class QRTest
 * 25/10/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class QRTest {
    @Test
    public void testDump() {
        File file = QRCodeGenerator.generateQR("http://www.nba.com"
                , GenericFileUtils.getResourcePath("testQR.png"));
        Assert.assertNotNull(file);
    }
}
