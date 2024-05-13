package com.lince.observer.transcoding;

import org.apache.commons.lang3.StringUtils;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * .app.component
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
     * @param fileUri file
     * @param pFrame  FFMpeg frame
     * @param width   image width
     * @param height  image height
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
     * @param pFrame frame to render
     * @param width  image size x
     * @param height image size y
     * @param f_idx  index for name pattern
     */
    public void saveFrameAsImage(AVFrame pFrame, int width, int height, int f_idx) {
        try {
            // Open file
            String szFilename = String.format("frame%d_.ppm", f_idx);
            String ppmFileUri = getClass().getResource("/").getPath() + szFilename;
            saveFrameAsPPM(ppmFileUri, pFrame, width, height);
            convertPPM2JPG(getClass().getResource("/").getPath(), szFilename);
        } catch (Exception e) {
            log.error("saveFrameAsImage", e);
        }
    }

    /**
     * Converts ppm file to jpg
     *
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
            File outputFile = new File(directory + StringUtils.replace(fileInputURI, ".ppm", ".jpg"));
            System.out.printf(outFormat, "output file", outputFile.getAbsolutePath());
            boolean writeSuccess = ImageIO.write(image, "JPEG", outputFile);
            System.out.printf(outFormat, "write successful", writeSuccess);
        } catch (Exception e) {
            log.error("MEK", e);
        }
    }

    /**
     * Gets a valid file path for the file in same directory or custom
     *
     * @param file     original video File
     * @param output   nullable. Desired output path
     * @param fileType media file type
     * @return valid path in system
     */
    public String getDefaultOutputPath(File file, String output, MediaFileType fileType) {
        try {
            String outputFile;
            String path = StringUtils.EMPTY;
            if (file != null) {
                File currentFileLocation = file.getAbsoluteFile();
                if (!currentFileLocation.isDirectory()) {
                    path = file.getAbsoluteFile().getParent();
                } else {
                    path = file.getAbsolutePath();
                }
            }
            if (StringUtils.isEmpty(output)) {
                outputFile = String.format("%s%s%s-conversion%s"
                        , path
                        , FileSystems.getDefault().getSeparator()
                        , StringUtils.substringBeforeLast(file.getName(), ".")
                        , fileType.getExtension());
            } else {
                outputFile = StringUtils.substringBeforeLast(output, ".") + fileType.getExtension();
            }
            return outputFile;
        } catch (Exception e) {
            return "./output" + fileType.getExtension();
        }
    }

    /**
     * Loads ffmpeg, and executes conversion to same file name patter + "-conversion.mp4" overwriting previous files
     *
     * @param file       original video file
     * @param outputPath override path
     * @return created file
     */
    public File transcodeFileToMp4(File file, String outputPath) {
        try {
            String outputFile = getDefaultOutputPath(file, outputPath, MediaFileType.MP4);
            String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
            ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-y", "-i", file.getAbsolutePath(), "-vcodec", "h264", outputFile);
            pb.inheritIO().start().waitFor();
            return new File(outputFile);
        } catch (Exception e) {
            log.error("Transcoding file", e);
        }
        return null;
    }

    /**
     * Transcode file to mp4 in same location
     *
     * @param file original file
     * @return new file
     */
    public File transcodeFileToMP4(File file) {
        return transcodeFileToMp4(file, StringUtils.EMPTY);
    }

    /**
     * Analyzes video and converts to MP4 if other format
     *
     * @param file selected file
     * @return same or new file with mp4 format
     */
    public File reviewVideoFile(File file, Predicate<String> actionIfNotMp4) {
        File rtn = file;
        try {
            if (file != null) {
                String format = StringUtils.substringAfterLast(file.getPath(), ".");
                if (!StringUtils.lowerCase(format).equals("mp4")) {
                    if (actionIfNotMp4.test(format)) {
                        return transcodeFileToMP4(file);
                    }
                    throw new RuntimeException("No conversion accepted");
                }
            }
        } catch (Exception e) {
            log.error("Reviewing file", e);
            throw e;
        }
        return rtn;
    }

    public String getFFMPEGInformationForFile(File file) {
        try {
            String ffprobe = Loader.load(org.bytedeco.ffmpeg.ffprobe.class);
            ProcessBuilder pb = new ProcessBuilder(ffprobe, file.getAbsolutePath());
            Process processDuration = pb.redirectErrorStream(true).start();
            StringBuilder strBuild = new StringBuilder();
            try (BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(processDuration.getInputStream(), Charset.defaultCharset()));) {
                String line;
                while ((line = processOutputReader.readLine()) != null) {
                    strBuild.append(line + System.lineSeparator());
                }
                processDuration.waitFor();
            }
            return strBuild.toString().trim();
        } catch (Exception e) {
            log.error("Getting file information", e);
            return StringUtils.EMPTY;
        }
    }

    public Optional<Integer> getFPSFromVideo(File file){
        if (file !=null){
            String informationValue = getFFMPEGInformationForFile(file);
            Integer position = StringUtils.indexOf(informationValue, "fps");
            if (StringUtils.isNotEmpty(informationValue) && position > 0){
                Double fps = Double.valueOf(StringUtils.trim(StringUtils.substringAfterLast( StringUtils.substringBeforeLast(informationValue,
                        "fps"),",")));
                return Optional.of(fps.intValue());
            }
        }
        return Optional.empty();
    }

   /*
   This is a way of consumer in java
   private String getFPS(File file, Consumer<File> consumer) {
        // Create a stream to hold the output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        // IMPORTANT: Save the old System.out!
        PrintStream old = System.out;
        // Tell Java to use your special stream
        System.setOut(ps);
        // Print some output: goes to your special stream
        consumer.accept(file);
        // Put things back
        System.out.flush();
        System.setOut(old);
        // Show what happened
        return baos.toString();
    }

    String result = getFPS(file, p);
    System.out.println(result);

    */
}
