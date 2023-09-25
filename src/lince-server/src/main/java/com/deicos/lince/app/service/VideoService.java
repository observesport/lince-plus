package com.deicos.lince.app.service;

import com.deicos.lince.app.ServerAppParams;
import com.deicos.lince.app.helper.ServerValuesHelper;
import com.deicos.lince.data.bean.VideoPlayerData;
import com.deicos.lince.data.util.JavaFXLogHelper;
import com.deicos.lince.math.service.DataHubService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.deicos.lince.app.ServerAppParams.*;
import static com.deicos.lince.app.ServerAppParams.BYTES;

/**
 * lince-scientific-desktop
 * com.deicos.lince.app.service
 *
 * @author berto (alberto.soto@gmail.com)in 22/06/2016.
 * Description:
 */
@Service
public class VideoService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static VideoPlayerData videoPlayerData = new VideoPlayerData();
    @Autowired
    private ResourceLoader resourceLoader;
    public static final String DEFAULT_VIDEO_EXAMPLE = "public/media/crono.mp4";

    @Autowired
    protected DataHubService dataHubService;

    public void addVideoFile(File f) {
        if (f != null)
            dataHubService.getVideoPlayList().add(f);
    }

    /**
     * Returns selected video File from a rq named
     *
     * @param fileRQ user selection name
     * @return File holding selectino
     */
    public File getSelectedVideoFile(String fileRQ) {
        try {
            File result = null;
            if (StringUtils.equals(fileRQ, "test")) {
                //The old version uses profileService, so lets get the damn rabbit into action
                result = getVideoFile();
            } else {
                for (File f : dataHubService.getVideoPlayList()) {
                    if (StringUtils.contains(f.getPath(), fileRQ)) {
                        result = f;
                    }
                }
            }
            return result;
        } catch (Exception e) {
            return getVideoFile();
        }
    }

    /**
     * Why? getting a file from classpath files outside of spring context in any OS breaks
     * <p>
     * A java.io.File represents a file on the file system, in a directory structure.
     * The Jar is a java.io.File. But anything within that file is beyond the reach of java.io.File.
     * As far as java is concerned, until it is uncompressed, a class in jar file is no different than
     * a word in a word document.
     * <p>
     * Font:  https://stackoverflow.com/questions/36371748/spring-boot-access-static-resources-missing-scr-main-resources
     * Font2: https://medium.com/@jonathan.henrique.smtp/reading-files-in-resource-path-from-jar-artifact-459ce00d2130
     * Font3: https://stackoverflow.com/questions/14876836/file-inside-jar-is-not-visible-for-spring
     *
     * @param path classpath uri
     * @return valid file from classpath
     */
    private File getResourceFile(String path) {
        try {
            String filename = StringUtils.substringBeforeLast(path, ".");
            String extension = StringUtils.substringAfterLast(path, ".");
            File file = File.createTempFile(filename, "." + extension);
            ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
            InputStream inputStream = ctx.getResource("classpath:" + path).getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, file);
            //Common way : File file = new File(getClass().getClassLoader().getResource(DEFAULT_VIDEO_EXAMPLE).getFile());
            //Way2 not working: File file = new ClassPathResource(path).getFile();
            return file;
        } catch (Exception e) {
            logger.error("Getting resource file", e);
            JavaFXLogHelper.addLogError("Getting resource file", e);
            return null;
        }
    }

    public void addAllFiles(List<File> file) {
        dataHubService.getVideoPlayList().clear();
        dataHubService.getVideoPlayList().addAll(file);
    }

    public HashMap<String, String> getPlaylistData() {
        HashMap<String, String> list = new HashMap<>();
        try {
            Tika tika = new Tika();
            for (File f : dataHubService.getVideoPlayList()) {
                String currentFileName = ServerValuesHelper.getHtmlFileName(f);
                //detecting the file type using detect method
                String type = tika.detect(f);
                list.put(currentFileName, type);
            }
            for (String f : dataHubService.getYoutubeVideoPlayList()) {
                list.put(f, ServerAppParams.YOUTUBE_VIDEO_TYPE);
            }
            if (list.isEmpty()) {
                File file = getResourceFile(DEFAULT_VIDEO_EXAMPLE);
                dataHubService.getVideoPlayList().setAll(file);
                return getPlaylistData();
            }
        } catch (Exception e) {
            JavaFXLogHelper.addLogError("UPS, problemas cargando lista de videos...", e);
        }
        return list;
    }

    /**
     * Return the url collection for rendering videos from remote connection via Spring Controller
     *
     * @return Video list to render
     */
    public List<VideoPlayerData> getRemotePlayListCollection() {
        List<VideoPlayerData> rtn = new ArrayList<>();
        try {
            for (Map.Entry<String, String> elem : this.getPlaylistData().entrySet()) {
                VideoPlayerData aux = new VideoPlayerData();
                if (StringUtils.equals(elem.getValue(), YOUTUBE_VIDEO_TYPE)){
                    aux.setUrl(elem.getKey());
                }else{
                    aux.setUrl(ServerAppParams.BASE_URL_STREAMING + elem.getKey());
                }
                aux.setEncoding(elem.getValue());
                rtn.add(aux);
            }
        } catch (Exception e) {
            JavaFXLogHelper.addLogError("UPS, problemas cargando lista de videos...", e);
        }
        return rtn;
    }

    public String getVideoUrlPath() {
        String realPath = StringUtils.substringBeforeLast(videoPlayerData.getUrl(), "/");
        return StringUtils.defaultIfEmpty(realPath, "/");
    }

    public String getVideoUrl() {
        if (StringUtils.isEmpty(videoPlayerData.getUrl())) {
            setLocal(false);
            return DEFAULT_VIDEO_EXAMPLE;
        }
        return videoPlayerData.getUrl();
    }

    public File getVideoFile() {
        try {
            File rtn;
            if (isLocal()) {
                rtn = getVideoPlayerData().getPath().toFile();
            } else {
                rtn = getResourceFile(getVideoUrl());
            }
            return rtn;
        } catch (Exception e) {
            logger.error("ERR accediendo fichero", e);
            return null;
        }
    }

    public VideoPlayerData getVideoPlayerData() {
        if (StringUtils.isEmpty(videoPlayerData.getUrl())) {
            setVideoUrl(getVideoUrl());
        }
        return videoPlayerData;
    }

    /**
     * OJO! se usa para consultar el punto de acceso. Se necesita mejorar
     * Por ahora si es "local" es que ha seleccionado un fichero de la forma convencional,
     * si no, es video por defecto
     *
     * @return
     */
    public boolean isLocal() {
        return videoPlayerData.isLocal();
    }

    public void setLocal(boolean isLocal) {
        videoPlayerData.setLocal(isLocal);
    }

    public void setVideoPlayerData(VideoPlayerData videoPlayerData) {
        VideoService.videoPlayerData = videoPlayerData;
    }

    public void setVideoUrl(String url) {
        videoPlayerData.setUrl(url);
    }

    public void setVideoURI(URI uri) {
        Path path = Paths.get(uri);
        videoPlayerData.setPath(path);
        videoPlayerData.setUrl(path.toString());
    }

    public Double getCurrentTime() {
        return videoPlayerData.getTime();
    }

    public void setCurrentTime(Double currentTime) {
        videoPlayerData.setTime(currentTime);
    }

    public Double getCurrentSpeed() {
        return videoPlayerData.getSpeed();
    }

    public void setCurrentSpeed(Double currentSpeed) {
        videoPlayerData.setSpeed(currentSpeed);
    }

//    https://melgenek.github.io/spring-video-service
    public ResponseEntity<byte[]> getStreamingResponse(File file, String httpRangeList) throws MalformedURLException {
        try {
            if (file != null) {
                byte[] data = null;
                final Long fileSize = FileUtils.sizeOf(file);
                UrlResource video = new UrlResource(file.toURI());
                String contentLength = fileSize.toString();
                long rangeStart = 0;
                long rangeEnd = CHUNK_SIZE;
                if (StringUtils.isNotEmpty(httpRangeList)) {
                    String[] ranges = httpRangeList.split("-");
                    rangeStart = Long.parseLong(ranges[0].substring(6));
                    if (ranges.length > 1) {
                        rangeEnd = Long.parseLong(ranges[1]);
                    } else {
                        rangeEnd = rangeStart + CHUNK_SIZE;
                    }
                    rangeEnd = Math.min(rangeEnd, fileSize - 1);
                    contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
                    data = readByteRange(video.getFile().getAbsolutePath(), rangeStart, rangeEnd);
                }
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(CONTENT_TYPE, VIDEO_CONTENT + "mp4")
                        .header(ACCEPT_RANGES, BYTES)
                        .header(CONTENT_LENGTH, StringUtils.isNotEmpty(httpRangeList)?contentLength:String.valueOf(fileSize))
                        .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                        .contentType(MediaTypeFactory
                                .getMediaType(video)
                                .orElse(MediaType.APPLICATION_OCTET_STREAM))
                        .body(data);
            } else {
                //if reach point, is not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("videoOutputError", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    public static byte[] readByteRange(String sourceFilePath, long startingOffset, Long length) throws IOException
    {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(sourceFilePath, "r"))
        {
            byte[] buffer = new byte[length.intValue()];
            randomAccessFile.seek(startingOffset);
            randomAccessFile.readFully(buffer);
            return buffer;
        }
    }

    /**
     * Content length.
     *
     * @param filePath String.
     * @return Long.
     */
    public Long getFileSize(String filePath) {
        return Optional.ofNullable(filePath)
                .map(file -> Paths.get(filePath)) //Paths.get(getFilePath(), file)
                .map(this::sizeFromFile)
                .orElse(0L);
    }

    /**
     * Getting the size from the path.
     *
     * @param path Path.
     * @return Long.
     */
    private Long sizeFromFile(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ioException) {
            logger.error("Error while getting the file size", ioException);
        }
        return 0L;
    }

    public Mono<Resource> getMonoVideo(String url){
        return Mono.fromSupplier(()->resourceLoader.
                getResource("file:"+url))   ;
    }

}
