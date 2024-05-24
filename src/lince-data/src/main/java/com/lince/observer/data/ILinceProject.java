package com.lince.observer.data;

import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;

import java.io.File;
import java.util.List;

public interface ILinceProject {
    public List<Criteria> getObservationTool();

    public void setObservationTool(List<Criteria> criteriaData);

    public List<LinceRegisterWrapper> getRegister();

    public void setRegister(List<LinceRegisterWrapper> register);

    public List<ResearchProfile> getProfiles();

    public void setProfiles(List<ResearchProfile> profiles);

    public List<File> getVideoPlayList();

    public void setVideoPlayList(List<File> videoPlayList);

    public List<String> getYoutubeVideoPlayList();

    public void setYoutubeVideoPlayList(List<String> youtubeVideoPlayList);
}
