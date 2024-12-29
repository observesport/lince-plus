package com.lince.observer.data.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.util.TimeCalculations;

import java.io.Serializable;
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
public class RegisterItem implements Comparable<RegisterItem>, Serializable {

    private Integer id;
    private Date saveDate;
    private Double videoTime;
    private String videoTimeTxt;
    protected String name;
    protected String description;
    private Integer frames = null;
    private List<Category> register = new ArrayList<>();
    TimeCalculations timeCalculations = new TimeCalculations();


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
        this.frames = Math.toIntExact((long) (getVideoTime() * VideoPlayerData.DEFAULT_FRAMES_PER_SECOND));
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

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
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
        return timeCalculations.getVideoTimeMilis(videoTime);
    }

    public void setVideoTime(Double videoTime) {
        this.videoTime = videoTime;
        setFramesFromTime();
        String secondsTxt = LocalTime.MIN.plusSeconds(videoTime.longValue()).toString();
        setVideoTimeTxt(secondsTxt);
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
        register.add(item);
    }

    @Override
    public int compareTo(RegisterItem o) {
        return this.videoTime.compareTo(o.videoTime);
    }

    @Override
    public String toString() {
        return String.valueOf(getVideoTime().toString());
    }
}
