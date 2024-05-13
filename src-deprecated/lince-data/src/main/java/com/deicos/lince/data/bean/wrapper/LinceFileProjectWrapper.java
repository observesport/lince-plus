package com.deicos.lince.data.bean.wrapper;

import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.data.bean.user.ResearchProfile;

import jakarta.xml.bind.annotation.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * lince-scientific-base
 * com.deicos.lince.data.bean.wrapper
 * Created by Alberto Soto Fernandez in 11/07/2017.
 * Description:
 * <p>
 * Content wrapper for any data in the application
 * Used to map and save in file through JAXBContext and Marshalling to XML
 */
@XmlRootElement(name = "linceProject") //namespace = "http://www.w3.org/2005/Atom",
@XmlAccessorType(XmlAccessType.FIELD)
public class LinceFileProjectWrapper {

    @XmlElementWrapper(name = "criterias")
    @XmlElement(name = "criteria")
    private List<Criteria> criteriaData = new ArrayList<>();

    @XmlElementWrapper(name = "registers")
    @XmlElement(name = "register")
    private List<LinceRegisterWrapper> register = new ArrayList<>();

    @XmlElementWrapper(name = "profiles")
    @XmlElement(name = "profileData")
    private List<ResearchProfile> profiles = new ArrayList<>();

    @XmlElementWrapper(name = "videoPlayList")
    @XmlElement(name = "video")
    private List<File> videoPlayList = new ArrayList<>();
    @XmlElementWrapper(name = "youtubeVideoPlayList")
    @XmlElement(name = "youtubeVideo")
    private List<String> youtubeVideoPlayList = new ArrayList<>();

    public List<Criteria> getCriteriaData() {
        return criteriaData;
    }

    public void setCriteriaData(List<Criteria> criteriaData) {
        this.criteriaData = criteriaData;
    }

    public List<LinceRegisterWrapper> getRegister() {
        return register;
    }

    public void setRegister(List<LinceRegisterWrapper> register) {
        this.register = register;
    }

    public List<ResearchProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<ResearchProfile> profiles) {
        this.profiles = profiles;
    }

    public List<File> getVideoPlayList() {
        return videoPlayList;
    }

    public void setVideoPlayList(List<File> videoPlayList) {
        this.videoPlayList = videoPlayList;
    }

    public List<String> getYoutubeVideoPlayList() {
        return youtubeVideoPlayList;
    }

    public void setYoutubeVideoPlayList(List<String> youtubeVideoPlayList) {
        this.youtubeVideoPlayList = youtubeVideoPlayList;
    }
}
