package com.deicos.lince.data;

import com.deicos.lince.data.base.EmptyLinceApp;
import com.deicos.lince.data.system.operations.LinceFileHelper;
import com.deicos.lince.data.system.operations.OSDetector;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OSTest {
    @Test
    public void testMacBrowser() {
        Runtime runtime = Runtime.getRuntime();
        String[] args2 =  {"osascript","-e","open location www.google.es"};
        System.out.println("fin");
        try{
            // Process process = runtime.exec(args2);
            Runtime.getRuntime().exec("open https://observesport.github.io/lince-plus/");
        }catch (Exception e){
            System.out.printf("err",e);
        }
        String str = "Junit is working fine";
        Assert.assertEquals("Junit is working fine",str);
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
        Assert.assertNotNull(supportedTypes);
    }


    @Test
    public void testOSSystem(){
        OSDetector os = new OSDetector();
        System.out.println(os.getOS());

    }




}
