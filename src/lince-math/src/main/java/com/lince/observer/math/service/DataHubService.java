package com.lince.observer.math.service;

import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.base.DataHubServiceBase;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.service.SessionService;
import com.lince.observer.math.AppParams;
import com.lince.observer.data.common.SessionDataAttributes;
import com.lince.observer.math.LinceWebContextHolder;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Class DataHubService
 *
 * @author berto (alberto.soto@gmail.com). 19/04/2018
 * <p>
 * Hub service middlepoint to control data between javafx and web
 * All services and file importer-exporter connect through here
 * <p>
 * .data.bean.wrapper.LinceFileProjectWrapper
 */
@Service
@DesktopQualifier
public class DataHubService extends DataHubServiceBase {

    private final SessionService sessionService;

    @Autowired
    public DataHubService(SessionService sessionService) {
        this.sessionService = sessionService;
    }


//    public void setDataRegister( List<LinceRegisterWrapper> dataRegister) {
//        DataHubService.dataRegister = dataRegister;
//    }

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
            HttpSession httpSession = LinceWebContextHolder.get().getSession();
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
