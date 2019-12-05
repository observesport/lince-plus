package com.deicos.lince.data;

import com.deicos.lince.data.barcode.QRCodeGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

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
        File file = QRCodeGenerator.generateQRCodeFileImage("http://www.nba.com");
        Assert.assertNotNull(file);
    }
}
