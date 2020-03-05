package com.deicos.lince.transcoding.trials.javacv;

import org.bytedeco.ffmpeg.global.avutil;
//import org.bytedeco.javacv.FFmpegFrameGrabber;
//import org.bytedeco.javacv.FFmpegFrameRecorder;
//import org.bytedeco.javacv.Frame;
//import org.bytedeco.javacv.FrameRecorder;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.URL;

/**
 * com.deicos.lince.transcoding
 * Class JavaCVLauncher
 * 27/02/2020
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class JavaCVLauncher {
/*
    public static void main(String... args) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            //InputStream input = classLoader.getResourceAsStream("/demo-file.wmv");
            InputStream input = new FileInputStream(ResourceUtils.getFile("demo-file.wmv"));
            URL url = classLoader.getResource("resources/output.mp4");
            OutputStream outputStream = new FileOutputStream(new File(url.toURI()));
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
            grabber.start();

            FrameRecorder recorder = new FFmpegFrameRecorder(outputStream, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
            recorder.setVideoCodecName("h262");
            recorder.setFormat("mp4");
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setVideoQuality(0);
            recorder.setAudioQuality(0);
            recorder.start();

            Frame frame;
            while ((frame = grabber.grabFrame()) != null) {
                recorder.setTimestamp(grabber.getTimestamp());
                recorder.record(frame);
            }

            recorder.stop();
            grabber.stop();
        } catch (Exception e) {

        }

    }*/
}
