package com.lince.observer.data;

import com.lince.observer.data.barcode.QRCodeGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * com.lince.observer.data
 * Class QRTest
 * 25/10/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class QRTest {
    @Test
    public void testDump() {
        File file = QRCodeGenerator.generateQRCodeFileImage("http://www.nba.com");
        Assertions.assertNotNull(file);
    }
}
