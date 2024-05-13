package com.deicos.lince.data.bean.wrapper;

import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.user.UserProfile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.UUID;

/**
 * com.deicos.lince.data.bean.wrapper
 * Class LinceRegisterWrapper
 *
 * @author berto (alberto.soto@gmail.com). 27/04/2018
 */
public class LinceRegisterWrapper {

    private UUID id;
    private ObservableList<RegisterItem> registerData = FXCollections.observableArrayList();
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
    public ObservableList<RegisterItem> getRegisterData() {
        return registerData;
    }

    public void setRegisterData(ObservableList<RegisterItem> registerData) {
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
