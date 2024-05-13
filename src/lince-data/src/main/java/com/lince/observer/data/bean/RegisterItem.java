package com.lince.observer.data.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lince.observer.data.bean.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * lince-scientific-desktop
 * com.lince.observer.data
 *
 * @author berto (alberto.soto@gmail.com)in 22/06/2016.
 * Description:
 */
public class RegisterItem implements Comparable<RegisterItem> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private Integer id;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private Date saveDate;
    private Double videoTime;
    private String videoTimeTxt;
    protected String name;
    protected String description;
    private Integer frames = null;
    private List<Category> register = new ArrayList<>();

    public String getVideoTimeTxt() {
        return videoTimeTxt;
    }

    public void setVideoTimeTxt(String videoTimeTxt) {
        this.videoTimeTxt = videoTimeTxt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public RegisterItem() {
        super();
        this.setSaveDate(new Date());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFrames() {
        if (frames == null) {
            setFramesFromTime();
        }
        return frames;
    }

    /**
     * Only use for migration purposes
     *
     * @param frames
     */
    public void setFrames(Integer frames) {
        this.frames = frames;
    }

    public void setFramesFromTime() {
        try {
            this.frames = Math.toIntExact((long) (getVideoTime() * VideoPlayerData.DEFAULT_FRAMES_PER_SECOND));
        } catch (Exception e) {
            log.error("Excepción en conversion de frames", e);
        }
    }

    public RegisterItem(Double videoTime) {
        this();
        this.setSaveDate(new Date());
        this.setVideoTime(videoTime);

    }

    public RegisterItem(Double videoTime, Category... categories) {
        this(videoTime);
        if (categories != null) {
            for (Category item : categories) {
                pushCategory(item);
            }
        }
    }

    public Date getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    public Double getVideoTime() {
        return videoTime;
    }

    public Integer getVideoTimeMilis() {
        return Math.toIntExact(getVideoTime().longValue() * 1000);
    }

    public void setVideoTime(Double videoTime) {
        this.videoTime = videoTime;
        //this.frames =  videoTime.intValue();//cambiar con el numero de frames por segundo del video
        try {
            setFramesFromTime();
            //TODO 2020: review: valid conversion to secs + min
            String secondsTxt = LocalTime.MIN.plusSeconds(videoTime.longValue()).toString();
            setVideoTimeTxt(secondsTxt);
        } catch (Exception e) {
            log.error("setVideoTime additional settings error (format time | frames)", e);
        }
    }

    public List<Category> getRegister() {
        return register;
    }

    public void setRegister(List<Category> register) {
        this.register = register;
    }

    /**
     * Introduce una categoria en el registro usando el codigo de ésta como clave o el id en caso contrario
     *
     * @param item categoría
     */
    public void pushCategory(Category item) {
        try {
            //StringUtils.defaultIfEmpty(item.getCode(), item.getId().toString()),
            register.add(item);
        } catch (Exception e) {
            log.error("pushCategory", e);
        }
    }

    @Override
    public int compareTo(RegisterItem o) {
        return this.videoTime.compareTo(o.videoTime);
        //return .compareTo(o.videoTime);
    }


    @Override
    public String toString() {
        return String.valueOf(getVideoTime().toString());
    }
}
