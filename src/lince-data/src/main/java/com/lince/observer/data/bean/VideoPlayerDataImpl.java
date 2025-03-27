package com.lince.observer.data.bean;

import java.nio.file.Path;
import java.util.UUID;

/**
 * lince-scientific-desktop
 * com.lince.observer.data
 * @author berto (alberto.soto@gmail.com)in 19/07/2016.
 * Description:
 */
public class VideoPlayerDataImpl implements VideoPlayerData {
    public boolean isLocal = false;
    public String id;
    public String url;
    public Path path;
    public Double time;
    public Double speed;

    public String encoding;

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public VideoPlayerDataImpl(){
        this.id = UUID.randomUUID().toString();
    }


    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Double getTime() {
        return time;
    }

    @Override
    public void setTime(Double time) {
        this.time = time;
    }

    @Override
    public Double getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    @Override
    public boolean isLocal() {
        return isLocal;
    }

    @Override
    public void setLocal(boolean local) {
        isLocal = local;
    }
}
