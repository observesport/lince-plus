package com.lince.observer.data.export;

import com.lince.observer.data.bean.RegisterItem;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

/**
 * com.lince.observer.data.export
 * Class Lince2ThemeExport
 * 12/06/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class Lince2ThemeExport {
    private static final Logger log = LoggerFactory.getLogger(Lince2ThemeExport.class);
    private List<RegisterItem> register;

    public Lince2ThemeExport(List<RegisterItem> register) {
        this.register = register;
    }

    public void createFile(FileWriter fileWriter){
        try {
            TsvWriter writer = new TsvWriter(fileWriter, new TsvWriterSettings());
            writer.writeHeaders("Time", "event");
            for (RegisterItem item : register) {
                String frame = item.getFrames().toString();
                String event = Arrays.toString(item.getRegister().toArray());
                event = org.apache.commons.lang3.StringUtils.remove(event, "[");
                event = org.apache.commons.lang3.StringUtils.remove(event, "]");
                writer.writeRow(frame, event);
            }
            writer.close();
        } catch (Exception e) {
            log.error("writing theme file", e);
        }
    }

    public void createFile(String url) {
        try {
            FileWriter fileWriter = new FileWriter(url + ".vvt");
            createFile(fileWriter);
        } catch (Exception e) {
            log.error("writing theme file", e);
        }

    }

}
