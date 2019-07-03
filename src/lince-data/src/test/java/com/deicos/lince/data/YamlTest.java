package com.deicos.lince.data;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * com.deicos.lince.data
 * Class YamlTest
 * 12/06/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class YamlTest {


    @Test
    public void testDump() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", "Silenthand Olleander");
        data.put("race", "Human");
        data.put("traits", new String[]{"ONE_HAND", "ONE_EYE"});
        Yaml yaml = new Yaml();
        String output = yaml.dump(data);
        System.out.println(output);
    }

    @Test
    public void testDumpWriter() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Silenthand Olleander");
        data.put("race", "Human");
        data.put("traits", new String[]{"ONE_HAND", "ONE_EYE"});
        Yaml yaml = new Yaml();
        StringWriter writer = new StringWriter();
        yaml.dump(data, writer);
        System.out.println(writer.toString());
    }

    @Test
    public void testDumpWriterFile() throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", "Silenthand Olleander");
        data.put("race", "Human");
        data.put("traits", new String[]{"ONE_HAND", "ONE_EYE"});

        Yaml yaml = new Yaml();
        FileWriter writer = new FileWriter(GenericFileUtils.getResourcePath("test.yaml"));
        yaml.dump(data, writer);
    }

    @Test
    public void testLoadFromStream() throws FileNotFoundException {
        InputStream input = new FileInputStream(new File(GenericFileUtils.getResourcePath("utf-8.txt")));
        Yaml yaml = new Yaml();
        Object data = yaml.load(input);
//        Assert.assertEquals("test", data);
        //
        data = yaml.load(new ByteArrayInputStream("test2".getBytes()));
  //      Assert.assertEquals("test2", data);
    }

    @Test
    public void testLoadManyDocuments() throws FileNotFoundException {
        InputStream input = GenericFileUtils.getInputReader("multipleDoc.yaml");
        Yaml yaml = new Yaml();
        int counter = 0;
        for (Object data : yaml.loadAll(input)) {
            System.out.println(data);
            counter++;
        }
        Assert.assertEquals(3, counter);
    }

    @Test
    public void testLoadFromString() {
        Yaml yaml = new Yaml();
        String document = "hello: 25";
        Map map = (Map) yaml.load(document);
        Assert.assertEquals("{hello=25}", map.toString());
//        Assert.assertEquals(new Long(25), map.get("hello"));
    }

    @Test
    public void testDumperOptions() throws IOException {
        DumperOptions options = new DumperOptions();
        options.setIndent(4);
        options.setSplitLines(true);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setCanonical(false);
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Silenthand Olleander");
        data.put("race", "Human");
        data.put("traits", new String[]{"ONE_HAND", "ONE_EYE"});

        Yaml yaml = new Yaml(options);
        FileWriter writer = GenericFileUtils.getWriter("dumperOptions.yaml");
        yaml.dump(data, writer);
    }
}
