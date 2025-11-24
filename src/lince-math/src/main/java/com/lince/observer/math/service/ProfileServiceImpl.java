package com.lince.observer.math.service;

import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.service.ProfileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * lince-scientific-desktop
 * .app.service
 *
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 * <p>
 * Mantains project profile info
 *
 * TODO 2019: se debe revisar para consolidar los usuarios
 *
 */
@Service
@DesktopQualifier
public class ProfileServiceImpl implements ProfileService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DataHubService dataHubService;

    @Autowired
    public ProfileServiceImpl(DataHubService dataHubService) {
        this.dataHubService = dataHubService;
    }

    @Override
    public List<ResearchProfile> getAllResearchInfo() {
        dataHubService.reloadResearchProfileDataFromRegister();
        return dataHubService.getUserData();
    }

    /**
     * @param user researchProfile
     */
    @Override
    public void setResearchProfile(ResearchProfile user) {
        ResearchProfile data;
        if (StringUtils.isEmpty(user.getKey().toString())) {
            data = new ResearchProfile();
            BeanUtils.copyProperties(data, user, "id");
        } else {
            data = user;
        }
        boolean isFound = false;
        for (ResearchProfile aux : dataHubService.getUserData()) {
            if (StringUtils.equals(aux.getKey().toString(), data.getKey().toString())) {
                BeanUtils.copyProperties(data, aux, "id");
                isFound = true;
            }
        }
        setUsersToRegister(data);
        if (!isFound) {
            dataHubService.getUserData().add(data);
        }
    }

    /**
     * Check the register for ids or creates them to be able to associate
     */
    private void checkRegisterIds() {
        for (LinceRegisterWrapper register : dataHubService.getDataRegister()) {
            if (register.getId() == null) {
                register.setId(UUID.randomUUID());
            }
        }
    }

    /**
     * checks all register has a user associated. If not they will be deleted later.
     *
     * @param data current researchProfileData
     */
    private void checkUserAssociation(ResearchProfile data) {
        //safe init
        data.getUserProfiles().forEach(o -> {
            if (o.getKey() == null) {
                o.setKey(UUID.randomUUID());
            }
            if (StringUtils.isEmpty(o.getUserName())) {
                o.setUserName("Usuario sin nombre");
            }
        });
        //safe check
        dataHubService.getDataRegister().forEach(o -> {
            if (o.getUserProfile() == null) {
                int currentPosition = dataHubService.getDataRegister().indexOf(o);
                if (data.getUserProfiles().get(currentPosition) != null) {
                    o.setUserProfile(data.getUserProfiles().get(currentPosition));
                }
            }
        });
    }

    /**
     * Upsert part: let's update current data if found or create new ones
     *
     * @param data current researchProfileData
     */
    private void upsertRegisters(ResearchProfile data) {
        //Upsert part: let's update current data if found or create new ones
        data.getUserProfiles().forEach(user -> {
            boolean isFound = false;
            for (LinceRegisterWrapper register : dataHubService.getDataRegister()) {
                if (register.getUserProfile().getKey().equals(user.getKey())) {
                    isFound = true;
                    register.setUserProfile(user);
                }
            }
            if (!isFound) {
                LinceRegisterWrapper register = new LinceRegisterWrapper();
                register.setUserProfile(user);
                dataHubService.getDataRegister().add(register);
            }
        });
    }

    /**
     * Remove part: let's clear register for non existing user
     *
     * @param data current researchProfileData
     */
    private void cleanNonUsedRegisters(ResearchProfile data) {
        //Remove part: let's clear register for non existing user
        List<LinceRegisterWrapper> deleteList = new ArrayList<>();
        dataHubService.getDataRegister().forEach(aux -> {
            boolean isFound = false;
            for (UserProfile user : data.getUserProfiles()) {
                if (aux.getUserProfile() != null && aux.getUserProfile().getKey().equals(user.getKey())) {
                    isFound = true;
                }
            }
            if (!isFound) {
                deleteList.add(aux);
            }
        });
        // Remove all items in deleteList from dataRegister
        // Using removeAll() works correctly with CopyOnWriteArrayList
        dataHubService.getDataRegister().removeAll(deleteList);
    }

    /**
     * Checks all user data with custom register info, and clears them after setting it up
     *
     * @param data researchProfile
     */
    private void setUsersToRegister(ResearchProfile data) {
        try {
            //Legacy for old register (double association)
            checkRegisterIds();
            checkUserAssociation(data);
            //Upsert part: let's update current data if found or create new ones
            upsertRegisters(data);
            //Remove part: let's clear register for non existing user
            cleanNonUsedRegisters(data);
        } catch (Exception e) {
            log.error("While full register check", e);
        }
        log.info("checkpoint");
    }



}
