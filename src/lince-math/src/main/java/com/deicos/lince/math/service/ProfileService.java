package com.deicos.lince.math.service;

import com.deicos.lince.data.bean.user.ResearchProfile;
import com.deicos.lince.data.bean.user.UserProfile;
import com.deicos.lince.data.bean.wrapper.LinceRegisterWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * lince-scientific-desktop
 * com.deicos.lince.app.service
 *
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 * <p>
 * Mantains project profile info
 */
@Service
public class ProfileService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DataHubService dataHubService;

    @Autowired
    public ProfileService(DataHubService dataHubService) {
        this.dataHubService = dataHubService;
    }

    public List<ResearchProfile> getAllResearchInfo() {
        reloadResearchProfileDataFromRegister();
        return dataHubService.getUserData();
    }

    /**
     * @param user researchProfile
     */
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
                if (aux.getUserProfile().getKey().equals(user.getKey())) {
                    isFound = true;
                }
            }
            if (!isFound) {
                deleteList.add(aux);
            }
        });
        //Let's avoid concurrent modification with iterator
        for (Iterator<LinceRegisterWrapper> it = dataHubService.getDataRegister().iterator(); it.hasNext(); ) {
            LinceRegisterWrapper aux = it.next();
            boolean isFound = false;
            for (LinceRegisterWrapper wrapper : deleteList) {
                if (wrapper.equals(aux)) {
                    isFound = true;
                }
            }
            if (isFound) {
                it.remove();
            }
        }
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

    /**
     * Gets research info, adding on top register users
     *
     * @return Fake researchProfile
     */
    private ResearchProfile reloadResearchProfileDataFromRegister() {
        ResearchProfile profile = null;
        try {
            if (dataHubService.getUserData().isEmpty()) {
                dataHubService.getUserData().add(new ResearchProfile());
            }
            profile = dataHubService.getUserData().get(0);
            //check registers and create research info asociate to it
            profile.getUserProfiles().clear();
            for (LinceRegisterWrapper item : dataHubService.getDataRegister()) {
                item.getUserProfile().setRegisterAmount(item.getRegisterData().size());
                profile.getUserProfiles().add(item.getUserProfile());
            }
        } catch (Exception e) {
            log.error("err current research", e);
        }
        return profile;
    }

}
