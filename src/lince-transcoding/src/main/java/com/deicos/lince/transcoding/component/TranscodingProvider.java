package com.deicos.lince.transcoding.component;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.javacpp.BytePointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * com.deicos.lince.app.component
 * Class TranscodingProvider
 * 11/12/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Component
public class TranscodingProvider {

    private static final Logger log = LoggerFactory.getLogger(TranscodingProvider.class);

    /**
     * Creates a PPM format file in desired file
     *
     * @param fileUri   file
     * @param pFrame FFMpeg frame
     * @param width  image width
     * @param height image height
     */
    public void saveFrameAsPPM(String fileUri, AVFrame pFrame, int width, int height) {

        try {
            // Open file
            OutputStream pFile = new FileOutputStream(fileUri);
            // Write header
            pFile.write(String.format("P6\n%d %d\n255\n", width, height).getBytes());
            // Write pixel data
            BytePointer data = pFrame.data(0);
            byte[] bytes = new byte[width * 3];
            int l = pFrame.linesize(0);
            for (int y = 0; y < height; y++) {
                data.position(y * l).get(bytes);
                pFile.write(bytes);
            }
            // Close file
            pFile.close();
        } catch (Exception e) {
            log.error("Creating frame file", e);
        }
    }

    /**
     *
     * @param pFrame
     * @param width
     * @param height
     * @param f_idx
     * @throws IOException
     */
    public void saveFrameAsImage(AVFrame pFrame, int width, int height, int f_idx) throws IOException {
        // Open file
        String szFilename = String.format("frame%d_.ppm", f_idx);
        String ppmFileUri = getClass().getResource("/").getPath() + szFilename;
        saveFrameAsPPM(ppmFileUri,pFrame,width,height);
        convertPPM2JPG(getClass().getResource("/").getPath(),szFilename);
    }
    /**
     * Converts ppm file to jpg
     * @param directory
     * @param fileInputURI
     */
    public void convertPPM2JPG(String directory, String fileInputURI) {
        try {
            String outFormat = "%-17s: %s%n";
            //Path inputFile = Paths.get(getClass().getResource("/").getPath(),fileInputURI);
            //InputStream is = Files.newInputStream(inputFile, StandardOpenOption.READ);
            InputStream is = new FileInputStream(directory + fileInputURI);
            BufferedImage image = ImageIO.read(is);
            //File outputFile = Paths.get(getClass().getResource("/").getPath(), "output.jpg").toAbsolutePath().toFile();
            File outputFile = new File(directory+ StringUtils.replace(fileInputURI, ".ppm",".jpg"));
            System.out.printf(outFormat, "output file", outputFile.getAbsolutePath());
            boolean writeSuccess = ImageIO.write(image, "JPEG", outputFile);
            System.out.printf(outFormat, "write successful", writeSuccess);
        } catch (Exception e) {
            log.error("MEK", e);
        }
    }

}
