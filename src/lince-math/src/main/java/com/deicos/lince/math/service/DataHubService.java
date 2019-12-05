package com.deicos.lince.math.service;

import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.data.bean.user.ResearchProfile;
import com.deicos.lince.data.bean.wrapper.LinceRegisterWrapper;
import com.deicos.lince.data.base.DataHubServiceBase;
import com.deicos.lince.math.SessionDataAttributes;
import com.deicos.lince.math.WebContextHolder;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
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




    /**
     * get current register saved in httpSession
     *
     * @return register with session uuid under register
     */
    @Override
    protected LinceRegisterWrapper getSessionRegister() {
        try {
            //Too messy passsing httpSession object everywhere. Let's get it in another way
            WebContextHolder contextHolder = WebContextHolder.get();
            List<LinceRegisterWrapper> dataRegister = getDataRegister();
            UUID defaultKeyRegister = dataRegister.get(0).getId();
            // Let's get or set a default context register
            String currentSession = sessionService.getSessionData(contextHolder.getSession()
                    , SessionDataAttributes.REGISTER
                    , defaultKeyRegister.toString());
            log.info("currentSesssion" + currentSession);
            if (currentSession != null) {
                for (LinceRegisterWrapper aux : dataRegister) {
                    if (aux.getId().equals(UUID.fromString(currentSession))) {
                        return aux;
                    }
                }
            }
        } catch (Exception e) {
            log.error("GETTING Session object", e);
        }
        return null;
    }






}
