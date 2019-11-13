package com.deicos.lince.app.service;

import com.deicos.lince.app.ServerAppParams;
import com.deicos.lince.data.util.JavaFXLogHelper;
import com.deicos.lince.app.helper.ServerValuesHelper;
import com.deicos.lince.data.bean.VideoPlayerData;
import com.deicos.lince.math.service.DataHubService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * lince-scientific-desktop
 * com.deicos.lince.app.service
 *
 * @author berto (alberto.soto@gmail.com)in 22/06/2016.
 * Description:
 */
@Service
public class VideoService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static VideoPlayerData videoPlayerData = new VideoPlayerData();

    public static final String DEFAULT_VIDEO_EXAMPLE = "public/media/crono.mp4";

    @Autowired
    protected DataHubService dataHubService;

    public void addVideoFile(File f) {
        if (f != null)
            dataHubService.getVideoPlayList().add(f);
    }

    public void addAllFiles(List<File> file) {
        dataHubService.getVideoPlayList().clear();
        dataHubService.getVideoPlayList().addAll(file);
    }

    public HashMap<String, String> getPlaylistData() {
        HashMap<String, String> list = new HashMap<>();
        try {
            Tika tika = new Tika();
            for (Object f : dataHubService.getVideoPlayList()) {
                File file = (File) f;
                String currentFileName = ServerValuesHelper.getHtmlFileName(file);
                //detecting the file type using detect method
                String type = tika.detect(file);
                list.put(currentFileName, type);
            }
            if (list.size() == 0) {
                File file = new File(
                        getClass().getClassLoader().getResource(DEFAULT_VIDEO_EXAMPLE).getFile()
                );
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
                aux.setUrl(ServerAppParams.BASE_URL_STREAMING + elem.getKey());
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
                ClassLoader classLoader = getClass().getClassLoader();
                rtn = new File(classLoader.getResource("public/" + getVideoUrl()).getFile());
            }
            return rtn;
        } catch (Exception e) {
            log.error("ERR accediendo fichero", e);
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
}
