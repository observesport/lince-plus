package com.lince.observer.data.base;

import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.util.List;
import java.util.Map;

/**
 * com.lince.observer.data.base
 * Class TsvFileHelper
 * 12/06/2019
 * Brings Theme software support for tab separeted files
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class TsvFileHelper {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<String, List<String>> data;

    public TsvFileHelper(Map<String, List<String>> data) {
        this.data = data;
    }

    public void write2File(String path) {
        try {
            FileWriter fileWriter = new FileWriter(path);
            TsvWriter tsvWriter = new TsvWriter(fileWriter, new TsvWriterSettings());
            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                tsvWriter.writeRow(entry.getKey());
                for (String cri : entry.getValue()) {
                    tsvWriter.writeRow(StringUtils.EMPTY, cri);
                }
            }
            tsvWriter.close();
        } catch (Exception e) {
            log.error("writting TSV File", e);
        }
    }

}
