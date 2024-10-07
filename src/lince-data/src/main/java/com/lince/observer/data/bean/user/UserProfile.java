package com.lince.observer.data.bean.user;

import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;

import java.io.Serializable;
import java.util.UUID;

/**
 * com.lince.observer.data.bean.user
 * Class UserProfile
 * @author berto (alberto.soto@gmail.com). 19/10/2018
 */
public class UserProfile implements Serializable {

    private UUID key;
    private String userName;
    private String email;
    private UUID registerCode;
    private int registerAmount;

    public UserProfile() {
        setKey(UUID.randomUUID());
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getRegisterCode() {
        return registerCode;
    }

    public void setRegisterCode(UUID registerCode) {
        this.registerCode = registerCode;
    }

    public int getRegisterAmount() {
        return registerAmount;
    }

    public void setRegisterAmount(int registerAmount) {
        this.registerAmount = registerAmount;
    }

    @Override
    public boolean equals(Object obj) {
        try{
            if (obj.getClass().isAssignableFrom(LinceRegisterWrapper.class)) {
                return ((LinceRegisterWrapper)obj).getId().equals(this.getRegisterCode());
            }
            if (obj.getClass().isAssignableFrom(UserProfile.class)) {
                return ((UserProfile)obj).getKey().equals(this.getKey());
            }
        }catch (Exception e){
            System.out.println("Error on equals");
        }
        return super.equals(obj);
    }
}
