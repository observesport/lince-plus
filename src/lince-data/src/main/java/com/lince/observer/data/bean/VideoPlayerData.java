package com.lince.observer.data.bean;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * Created by Alberto Soto. 27/3/25
 */
public interface VideoPlayerData extends Serializable {
    Integer DEFAULT_FRAMES_PER_SECOND = 21;

    String getEncoding();

    void setEncoding(String encoding);

    Path getPath();

    void setPath(Path path);

    String getUrl();

    void setUrl(String url);

    Double getTime();

    void setTime(Double time);

    Double getSpeed();

    void setSpeed(Double speed);

    boolean isLocal();

    void setLocal(boolean local);
}
