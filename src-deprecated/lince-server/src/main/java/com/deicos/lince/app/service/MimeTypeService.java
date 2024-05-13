package com.deicos.lince.app.service;

/**
 * lince-scientific-desktop
 * com.deicos.lince.app.service
 * @author berto (alberto.soto@gmail.com)in 22/07/2016.
 * Description:
 */

import com.google.common.collect.Maps;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Created by kevin on 16/07/15 for Podcast Server
 */
//@Transactional JPA eliminado
@Service
public class MimeTypeService {

    private final Map<String, String> mimeMap;
    private final TikaProbeContentType tikaProbeContentType = new TikaProbeContentType();


    public MimeTypeService() {
        //this.tikaProbeContentType = tikaProbeContentType;
        mimeMap = Maps.newHashMap();
        mimeMap.put("mp4", "video/mp4");
        mimeMap.put("ogv", "video/ogg");
        mimeMap.put("mp3", "audio/mp3");
        mimeMap.put("flv", "video/flv");
        mimeMap.put("webm", "video/webm");
        mimeMap.put("", "video/mp4");
    }

    public Optional<String> getMimeType(String extension) {
        if (extension.isEmpty())
            return Optional.of("application/octet-stream");

        if (mimeMap.containsKey(extension)) {
            return Optional.of(mimeMap.get(extension));
        } else {
            return Optional.of("unknown/" + extension);
        }
    }
/*
    public String getExtension(Item item) {
        if (item.getMimeType() != null) {
            return item.getMimeType().replace("audio/", ".").replace("video/", ".");
        }

        if ("Youtube".equals(item.getPodcast().getType()) || item.getUrl().lastIndexOf(".") == -1 ) {
            return ".mp4";
        } else {
            return "."+FilenameUtils.getExtension(item.getUrl());
        }
    }*/

    // https://odoepner.wordpress.com/2013/07/29/transparently-improve-java-7-mime-type-recognition-with-apache-tika/
    public String probeContentType(Path file) {
        return getMimeType(FilenameUtils.getExtension(String.valueOf(file.getFileName()))).toString();
        /*
        return filesProbeContentType(file)
                .orElseGet(() -> getMimeType(FilenameUtils.getExtension(String.valueOf(file.getFileName()))))
                    .orElseGet(() -> tikaProbeContentType.probeContentType(file)) ;
                    */
    }

    private Optional<String> filesProbeContentType(Path file) {
        String mimeType = null;

        try {
            mimeType = Files.probeContentType(file);
        } catch (IOException ignored) {
        }

        return Optional.ofNullable(mimeType);
    }


    public static class TikaProbeContentType {

        private final Tika tika = null;

        Optional<String> probeContentType(Path file) {
            try {
                return Optional.of(tika.detect(file.toFile()));
            } catch (IOException ignored) {
                return Optional.empty();
            }
        }
    }
}