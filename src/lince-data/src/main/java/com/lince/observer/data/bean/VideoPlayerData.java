package com.lince.observer.data.bean;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.UUID;

/**
 * lince-scientific-desktop
 * com.lince.observer.data
 * @author berto (alberto.soto@gmail.com)in 19/07/2016.
 * Description:
 */
public class VideoPlayerData implements Serializable {
    public static final Integer DEFAULT_FRAMES_PER_SECOND = 21;
    public boolean isLocal = false;
    public String id;
    public String url;
    public Path path;
    public Double time;
    public Double speed;

    public String encoding;

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public VideoPlayerData(){
        this.id = UUID.randomUUID().toString();
    }


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }
}
