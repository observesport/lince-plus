package com.deicos.lince.data.base;

import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.data.bean.user.ResearchProfile;
import com.deicos.lince.data.bean.user.UserProfile;
import com.deicos.lince.data.bean.wrapper.LinceRegisterWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lince.modelo.Registro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
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

    /**
     * Adds new observations checking if does not exists in the previous register
     * TODO 2019 check
     *
     * @param newItems
     */
    public void addDataRegister(List<LinceRegisterWrapper> newItems) {
        List<LinceRegisterWrapper> baseItems = getDataRegister();
        //for (LinceRegisterWrapper oldReg:baseItems) {
        for (LinceRegisterWrapper newReg : newItems) {
            //if(oldRegs.getId().equals(newReg.getId())){
            newReg.setId(UUID.randomUUID());
            newReg.getUserProfile().setKey(UUID.randomUUID());
            newReg.getUserProfile().setRegisterCode(newReg.getId());
            addObserver(newReg.getUserProfile());
            //}
        }
        //}
        baseItems.addAll(newItems);
    }

    /**
     * Checks if all data register have it's demanding user profile in the register
     */
    public void addObserver(UserProfile profile) {
        boolean hasAdd = false;
        for (ResearchProfile project : getUserData()) {
            //boolean isFound = false;
            for (UserProfile user : project.getUserProfiles()) {
                if (user.getKey().equals(profile.getKey())) {
                    //isFound = true;
                    profile.setKey(UUID.randomUUID());
                }
            }
            if (!hasAdd) {
                project.getUserProfiles().add(profile);
                hasAdd = true;
            }
        }
    }

    public ObservableList<Criteria> getCriteria() {
        return criteria;
    }

    public ObservableList<ResearchProfile> getUserData() {
        return userData;
    }

    /**
     * Returns the first researchProfile making a valid safe init
     *
     * @return Current researchProfile
     */
    public ResearchProfile getResearchProfile() {
        ResearchProfile profile = null;
        try {
            if (getUserData().isEmpty()) {
                getUserData().add(new ResearchProfile());
            }
            profile = getUserData().get(0);
            //check registers and create research info asociate to it
            if (profile.getUserProfiles() == null) {
                profile.setUserProfiles(new ArrayList<>());
            }
        } catch (Exception e) {
            log.error("getting researchProfile", e);
        }
        return profile;
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
                //log.info("Data register for session - returning first register by default");
            }
        } catch (Exception e) {
            log.error("on session data access", e);
        }
        //save return
        if (register == null) {
            return null;
        }
        return register.getRegisterData();
    }

    public UserProfile getCurrentUser() {
        try {
            return getSessionRegister().getUserProfile();
        } catch (Exception e) {
            return new UserProfile();
        }
    }

    public void clearData() {
        Registro.loadNewInstance();
        dataRegister.clear();
        criteria.clear();
        videoPlayList.clear();
        userData.clear();
    }


    /**
     * Gets research info, adding on top register users
     *
     * @return researchProfile built with registers from the system
     */
    public void reloadResearchProfileDataFromRegister() {
        ResearchProfile profile;
        try {
            profile = getResearchProfile();
            profile.getUserProfiles().clear();
            for (LinceRegisterWrapper item : getDataRegister()) {
                item.getUserProfile().setRegisterAmount(item.getRegisterData().size());
                profile.getUserProfiles().add(item.getUserProfile());
            }
        } catch (Exception e) {
            log.error("err current research", e);
        }
    }
}
