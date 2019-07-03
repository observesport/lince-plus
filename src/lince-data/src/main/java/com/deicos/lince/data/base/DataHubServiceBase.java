package com.deicos.lince.data.base;

import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.data.bean.user.ResearchProfile;
import com.deicos.lince.data.bean.wrapper.LinceRegisterWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * com.deicos.lince.data.util
 * Class DataHubServiceBase
 * 12/04/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class DataHubServiceBase {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * All data as observable to fit with javafx requirements
     */
    protected static ObservableList<LinceRegisterWrapper> dataRegister = FXCollections.observableArrayList();
    protected static ObservableList<Criteria> criteria = FXCollections.observableArrayList();
    protected static ObservableList<ResearchProfile> userData = FXCollections.observableArrayList();
    protected static ObservableList<File> videoPlayList = FXCollections.observableArrayList();

    public ObservableList<LinceRegisterWrapper> getDataRegister() {
        if (dataRegister == null || dataRegister.isEmpty()) {
            dataRegister.add(new LinceRegisterWrapper());
        }
        return dataRegister;
    }

    public ObservableList<Criteria> getCriteria() {
        return criteria;
    }

    public ObservableList<ResearchProfile> getUserData() {
        return userData;
    }

    public ObservableList<File> getVideoPlayList() {
        return videoPlayList;
    }

    protected <T> void setDataCollection(ObservableList<T> collection, List<T> item) {
        collection.clear();
        collection.setAll(item);
    }

    protected LinceRegisterWrapper getSessionRegister() {
        return null;
    }

    /**
     * Searchs a register by id
     *
     * @param uuid register id
     * @return register
     */
    public LinceRegisterWrapper getRegisterById(UUID uuid) {
        try {
            for (LinceRegisterWrapper aux : getDataRegister()) {
                if (aux.getId().equals(uuid)) {
                    return aux;
                }
            }
        } catch (Exception e) {
            log.error("searching register", e);
        }
        return null;
    }

    /**
     * Search active register or return first in case none is selected
     *
     * @return Last active register for user
     */
    public ObservableList<RegisterItem> getCurrentDataRegister() {
        LinceRegisterWrapper register = null;
        try {
            //if there isn't any, create it
            if (getDataRegister().isEmpty()) {
                getDataRegister().add(new LinceRegisterWrapper());
            }
            register = getSessionRegister();
            if (register == null) {
                //save set
                register = getDataRegister().get(0);
            }
        } catch (Exception e) {
            log.error("on session data access", e);
        }
        log.info("Data register for session - returning first register by default");
        //save return
        if (register == null) {
            return null;
        }
        return register.getRegisterData();
    }


    public void clearData() {
        dataRegister.clear();
        criteria.clear();
        videoPlayList.clear();
        userData.clear();
    }
}
