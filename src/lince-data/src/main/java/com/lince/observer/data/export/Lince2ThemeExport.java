package com.lince.observer.data.export;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

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
            writer.writeHeaders("TIME", "EVENT");
            boolean isFirst = true;
            int lastFrame = 0;
            for (RegisterItem item : register) {
                String frame = item.getFrames().toString();
                String event = item.getRegister().stream()
                    .map(c -> c.getCode())
                    .collect(Collectors.joining(","));
                if (isFirst) {
                    int startFrame = item.getFrames() - 1;
                    writer.writeRow(String.valueOf(startFrame), ":");
                    isFirst = false;
                }
                writer.writeRow(frame, event);
                lastFrame = item.getFrames();
            }
            if (!register.isEmpty()) {
                writer.writeRow(String.valueOf(lastFrame + 1), "&");
            }
            writer.close();
        } catch (Exception e) {
            log.error("writing theme file", e);
        }
    }

    public void createFile(String basePath) {
        try {
            FileWriter fileWriter = new FileWriter(basePath + ".txt");
            createFile(fileWriter);
        } catch (Exception e) {
            log.error("writing theme file", e);
        }
    }

    public void createFile(String basePath, List<Criteria> criteria) {
        try {
            createFile(new FileWriter(basePath + ".txt"));
            String vvtContent = createVvtContent(criteria);
            Files.write(Path.of(basePath + ".vvt"), vvtContent.getBytes());
        } catch (Exception e) {
            log.error("writing theme paired files", e);
        }
    }

    public static String createVvtContent(List<Criteria> criteria) {
        StringBuilder sb = new StringBuilder();
        for (Criteria c : criteria) {
            sb.append(c.getName()).append("\r\n");
            for (Category cat : c.getInnerCategories()) {
                sb.append(" ").append(cat.getCode()).append("\r\n");
            }
        }
        return sb.toString();
    }

}
