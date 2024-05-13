package com.lince.observer.data.service;

import com.lince.observer.data.bean.user.ResearchProfile;
import java.util.List;

/**
 * Created by Alberto Soto. 30/3/24
 */
public interface ProfileService {
    List<ResearchProfile> getAllResearchInfo();

    void setResearchProfile(ResearchProfile user);

}
