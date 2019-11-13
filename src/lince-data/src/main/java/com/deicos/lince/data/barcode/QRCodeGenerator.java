package com.deicos.lince.data.barcode;

import com.deicos.lince.data.BeanSchemeHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

/**
 * com.deicos.lince.data.barcode
 * Class QRCodeGenerator
 * 25/10/2019
 * Tutorial: http://zxing.github.io/zxing/apidocs/index.html
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class QRCodeGenerator {

    protected static final Logger log = LoggerFactory.getLogger(BeanSchemeHelper.class);

    /**
     * Creates a QR Code
     *
     * @param url  link to create
     * @param path file location
     * @return File on location
     */
    public static File generateQR(String url, String path) {
        String filePath = path;
        if (!StringUtils.endsWith(filePath, ".png")) {
            filePath += "/tempQR.png";
        }

        int size = 250;
        String fileType = "png";
        File myFile = new File(filePath);
        try {
            Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // Now with zxing version 3.2.1 you could change border size (white border size to just 1)
            hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, size, size, hintMap);
            int crunchifyWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(crunchifyWidth, crunchifyWidth, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, crunchifyWidth, crunchifyWidth);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < crunchifyWidth; i++) {
                for (int j = 0; j < crunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            ImageIO.write(image, fileType, myFile);
        } catch (Exception e) {
            log.error("QR generation", e);
            return null;
        }
        return myFile;
    }
}
