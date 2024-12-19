package com.lince.observer.data.component;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LinceDataConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface ILinceFileExporter {

    Logger log = LoggerFactory.getLogger(ILinceFileExporter.class);

    List<String> getDefaultColumnDefinitions(ILinceProject linceProject);

    default List<String> getUserSelectedColumnDefinitino(ILinceProject linceProject, List<String> columnDefinitions){
        List<String> header = getDefaultColumnDefinitions(linceProject);
        if (columnDefinitions != null && !columnDefinitions.isEmpty()) {
            List<String> filteredColumnDefinitions = columnDefinitions.stream()
                    .filter(header::contains)
                    .toList();
            header.clear();
            header.addAll(filteredColumnDefinitions);
        }
        return header;
    }

    String getFileFormat();

    default String executeFormatConversion(ILinceProject linceProject){
        return executeFormatConversion(linceProject, getUserSelectedColumnDefinitino(linceProject, getDefaultColumnDefinitions(linceProject)));
    }

    String executeFormatConversion(ILinceProject linceProject, List<String> columnDefinitions);

    default Double getFps(ILinceProject linceProject) {
        try {
            return Optional.ofNullable(linceProject)
                    .map(ILinceProject::getProfiles)
                    .filter(profiles -> !profiles.isEmpty())
                    .flatMap(profiles -> profiles.stream().findFirst())
                    .map(profile -> {
                        double fps = profile.getFps();
                        return fps > 0 ? fps : LinceDataConstants.DEFAULT_FPS;
                    })
                    .orElse(LinceDataConstants.DEFAULT_FPS);
        } catch (Exception e) {
            return LinceDataConstants.DEFAULT_FPS;
        }
    }

    default ByteArrayOutputStream save(ILinceProject linceProject) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            String serialization = executeFormatConversion(linceProject);
            baos.write(serialization.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Error writing serialization to ByteArrayOutputStream", e);
        }
        return baos;
    }


    default File saveAs(ILinceProject linceProject, String url) {
        if (url == null || url.isEmpty()) {
            log.error("Invalid URL provided");
            return null;
        }
        String fileFormat = getFileFormat();
        if (!url.toLowerCase().endsWith("." + fileFormat.toLowerCase())) {
            url += "." + fileFormat;
        }
        File file = new File(url);
        try {
            Files.createDirectories(Paths.get(file.getParent()));
            ByteArrayOutputStream baos = save(linceProject);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                baos.writeTo(fos);
            }
            log.info("File saved successfully: {}", file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            log.error("Error saving file: {}", url, e);
            return null;
        }
    }

}
