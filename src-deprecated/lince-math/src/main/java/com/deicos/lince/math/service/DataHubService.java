package com.deicos.lince.math.service;

import com.deicos.lince.data.base.DataHubServiceBase;
import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.data.bean.user.ResearchProfile;
import com.deicos.lince.data.bean.wrapper.LinceRegisterWrapper;
import com.deicos.lince.math.AppParams;
import com.deicos.lince.math.SessionDataAttributes;
import com.deicos.lince.math.WebContextHolder;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * com.deicos.lince.math.service
 * Class DataHubService
 *
 * @author berto (alberto.soto@gmail.com). 19/04/2018
 * <p>
 * Hub service middlepoint to control data between javafx and web
 * All services and file importer-exporter connect through here
 * <p>
 * com.deicos.lince.data.bean.wrapper.LinceFileProjectWrapper
 */
@Service
public class DataHubService extends DataHubServiceBase {

    private final SessionService sessionService;

    @Autowired
    public DataHubService(SessionService sessionService) {
        this.sessionService = sessionService;
    }


    public void setDataRegister(ObservableList<LinceRegisterWrapper> dataRegister) {
        DataHubService.dataRegister = dataRegister;
    }

    public void setVideoPlayList(List<File> items) {
        setDataCollection(DataHubService.videoPlayList, items);
    }

    public void setCriteria(List<Criteria> items) {
        setDataCollection(DataHubService.criteria, items);
    }

    public void setUserData(List<ResearchProfile> items) {
        setDataCollection(DataHubService.userData, items);
    }


    public void addVideoItem(File file, Optional<Integer> fps) {
        getVideoPlayList().add(file);
        if (fps.isPresent()) {
            getResearchProfile().setFps(fps.get());
        }
        updateUserPlayList();
    }

    public void addYoutubeVideoItem(String link) {
        if (StringUtils.containsIgnoreCase(link, AppParams.YOUTUBE_SERVER)) {
            getYoutubeVideoPlayList().add(link);
        }
        updateUserPlayList();
    }


    /**
     * get current register saved in httpSession
     *
     * @return register with session uuid under register
     */
    @Override
    protected LinceRegisterWrapper getSessionRegister() {
        try {
            //Too messy passsing httpSession object everywhere. Let's get it in another way
            HttpSession httpSession = WebContextHolder.get().getSession();
            List<LinceRegisterWrapper> dataRegister = getDataRegister();
            UUID defaultKeyRegister = dataRegister.get(0).getId();
            // Let's get or set a default context register
            String currentSession = sessionService.getSessionData(httpSession
                    , SessionDataAttributes.REGISTER
                    , defaultKeyRegister.toString());
            log.info(String.format("Current session (%s) has selected register %s", httpSession.getId(), currentSession));
            if (currentSession != null) {
                for (LinceRegisterWrapper aux : dataRegister) {
                    if (aux.getId().equals(UUID.fromString(currentSession))) {
                        return aux;
                    }
                }
            }
        } catch (Exception e) {
            log.info("ERR GETTING Session object" + e.toString());
        }
        return null;
    }


}
