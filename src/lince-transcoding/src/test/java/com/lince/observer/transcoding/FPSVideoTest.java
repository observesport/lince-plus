package com.lince.observer.transcoding;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;

/**
 * Class TranscodingTest
 * 11/12/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
//@RunWith(SpringRunner.class) junit4
@ExtendWith(SpringExtension.class)    //junit5
@SpringBootTest(classes = {TranscodingProvider.class})
public class FPSVideoTest {

    public String getDemoFileURI(MediaFileType mediaFileType) {
        return String.format("/demo-file.%s", StringUtils.lowerCase(mediaFileType.toString()));
    }

    public File getDemoFile(MediaFileType mediaFileType) {
        String uri = getDemoFileURI(mediaFileType);
        return new File(getClass().getResource(uri).getFile());
    }

    @Autowired
    private TranscodingProvider transcodingProvider;

    private static final Logger log = LoggerFactory.getLogger(FPSVideoTest.class);

    @Test
    public void getInfo() {
        File file = getDemoFile(MediaFileType.OGG);
        String result = transcodingProvider.getFFMPEGInformationForFile(file);
        Assertions.assertNotNull(result);
    }

    @Test
    public void getFPS() {
        File file = getDemoFile(MediaFileType.OGG);
        Integer fps = transcodingProvider.getFPSFromVideo(file).get();
        Assertions.assertNotEquals(fps,-1);
    }
}
