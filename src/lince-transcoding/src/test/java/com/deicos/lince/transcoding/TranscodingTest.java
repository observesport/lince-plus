package com.deicos.lince.transcoding;

import com.deicos.lince.transcoding.component.TranscodingProvider;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacpp.Loader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

/**
 * com.deicos.lince.app.test
 * Class TranscodingTest
 * 11/12/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {com.deicos.lince.transcoding.component.TranscodingProvider.class})
public class TranscodingTest {

    public String getDemoFileURI(MediaFileType mediaFileType) {
        return String.format("/demo-file.%s", StringUtils.lowerCase(mediaFileType.toString()));
    }

    public File getDemoFile(MediaFileType mediaFileType) {
        String uri = getDemoFileURI(mediaFileType);
        return new File(getClass().getResource(uri).getFile());
    }

    @Autowired
    private TranscodingProvider transcodingProvider;

    private static final Logger log = LoggerFactory.getLogger(TranscodingTest.class);

    //@Test
    public void providerTranscoderConversion() {
        File tmp = getDemoFile(MediaFileType.OGG);
        File output = transcodingProvider.transcodeFileToMP4(tmp);
        log.info("output:"+output.getAbsolutePath());
        Assert.assertNotNull(output);
    }

    @Test
    public void getInfo() {
        try {
            File file = getDemoFile(MediaFileType.OGG);
            String ffprobe = Loader.load(org.bytedeco.ffmpeg.ffprobe.class);
            ProcessBuilder pb = new ProcessBuilder(ffprobe, file.getAbsolutePath());
            pb.inheritIO().start().waitFor();
        } catch (Exception e) {
            log.error("MEK", e);
        }
    }




}
