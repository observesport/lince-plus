package com.lince.observer.data;

import com.lince.observer.data.system.operations.OSDetector;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class OSTest {
    @Test
    public void testMacBrowser() {
        Runtime runtime = Runtime.getRuntime();
        String[] args2 =  {"osascript","-e","open location www.google.es"};
        System.out.println("fin");
        try{
            // Process process = runtime.exec(args2);
            Runtime.getRuntime().exec("open http://www.lince-plus.com");
        }catch (Exception e){
            System.out.printf("err",e);
        }
        String str = "Junit is working fine";
        Assertions.assertEquals("Junit is working fine",str);
    }

    @Test
    public void testSupportedVideoFiles() {
        String txt = "Video tipo ";
        List<Pair<String,String>> supportedTypes = new ArrayList<>();
        for (String type : StringUtils.split(LinceDataConstants.SUPPORTED_VIDEO_FILES, ";")) {
            supportedTypes.add(new MutablePair<>(txt+type,type));
        }
        System.out.println("ok");
       // List<File> fileList = LinceFileHelper.openMultipleFileDialog(new EmptyLinceApp(), supportedTypes);
        Assertions.assertNotNull(supportedTypes);
    }


    @Test
    public void testOSSystem(){
        OSDetector os = new OSDetector();
        System.out.println(os.getOS());

    }




}
