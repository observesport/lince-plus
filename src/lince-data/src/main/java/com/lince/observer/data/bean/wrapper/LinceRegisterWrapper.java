package com.lince.observer.data.bean.wrapper;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.user.UserProfile;
import jakarta.xml.bind.annotation.XmlElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * com.lince.observer.data.bean.wrapper
 * Class LinceRegisterWrapper
 *
 * @author berto (alberto.soto@gmail.com). 27/04/2018
 */
public class LinceRegisterWrapper implements Serializable {

    private UUID id;
    private List<RegisterItem> registerData = new ArrayList<>();
    private UserProfile userProfile = new UserProfile();

    public LinceRegisterWrapper() {
        setId(UUID.randomUUID());
        setUserProfile(new UserProfile());
    }

    @XmlElement(name = "id")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @XmlElement(name = "registerItem")
    public List<RegisterItem> getRegisterData() {
        return registerData;
    }

    public void setRegisterData(List<RegisterItem> registerData) {
        this.registerData = registerData;
    }

    public UserProfile getUserProfile() {
        doUserProfileKeySet(this.userProfile);
        return this.userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        doUserProfileKeySet(userProfile);
        this.userProfile = userProfile;

    }

    /**
     * Assigns the current register key to the internal register to ease search by key later
     *
     * @param userProfile any internal user profile data
     * @return modified version of the user profile with this register code as string
     */
    private UserProfile doUserProfileKeySet(UserProfile userProfile) {
        if (userProfile != null && userProfile.getRegisterCode()==null) {
            userProfile.setRegisterCode(this.getId());
        }
        return userProfile;
    }

    /**
     * Checks registers by uuid
     * @param obj uuid or register
     * @return same or not
     */
    @Override
    public boolean equals(Object obj) {
        try{
            if (obj.getClass().isAssignableFrom(LinceRegisterWrapper.class)) {
                return ((LinceRegisterWrapper)obj).getId().equals(this.getId());
            }
            if (obj.getClass().isAssignableFrom(UUID.class)){
                return obj.equals(this.getId());
            }
        }catch (Exception e){
            System.out.println("Error on equals");
        }
        return super.equals(obj);
    }
}
