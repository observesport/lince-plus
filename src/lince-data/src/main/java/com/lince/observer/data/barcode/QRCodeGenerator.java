package com.lince.observer.data.barcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

/**
 * com.lince.observer.data.barcode
 * Class QRCodeGenerator
 * 25/10/2019
 * Tutorial: http://zxing.github.io/zxing/apidocs/index.html
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class QRCodeGenerator {
    protected static final Logger log = LoggerFactory.getLogger(QRCodeGenerator.class);

    private static String getFilePath() {
        String userhome = System.getProperty("user.home");
        log.info("QRCODE homepath:" + userhome);
        return userhome + "/linceQRCode.png";
    }

    public static ByteArrayOutputStream getQRCodeImage() {
        try {
            ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
            BufferedImage image = ImageIO.read(new File(getFilePath()));
            ImageIO.write(image, "png", jpegOutputStream);
            return jpegOutputStream;
        } catch (Exception e) {
            log.error("getting QRCODE Image", e);
        }
        return null;
    }

    /**
     * Creates the QR Code with the desired size and stores it in the file system
     *
     * @param url  uri
     * @param size img size
     * @return valid buffer Image
     */
    private static BufferedImage genQRImage(String url, int size) {
        try {
            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // Now with zxing version 3.2.1 you could change border size (white border size to just 1)
            hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, size, size, hintMap);
            int crunchifySize = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(crunchifySize, crunchifySize, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, crunchifySize, crunchifySize);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < crunchifySize; i++) {
                for (int j = 0; j < crunchifySize; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            return image;
        } catch (Exception e) {
            log.error("QR image generation", e);
            return null;
        }
    }

    /**
     * Creates a QR Code
     *
     * @param url link to create
     * @return File on location
     */
    public static File generateQRCodeFileImage(String url) {
        try {
            String filePath = getFilePath();
            int size = 250;
            String fileType = "png";
            File myFile = new File(filePath);
            BufferedImage image = genQRImage(url, size);
            if (image != null) {
                ImageIO.write(image, fileType, myFile);
                return myFile;
            }
        } catch (Exception e) {
            log.error("generating file for QR image", e);
        }
        return null;
    }
}
