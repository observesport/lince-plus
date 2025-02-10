package com.lince.observer.data.service;

import com.lince.observer.data.bean.user.ResearchProfile;
import java.util.List;

import static com.lince.observer.data.util.TimeCalculations.DEFAULT_FPS;

/**
 * Created by Alberto Soto. 30/3/24
 */
public interface ProfileService {
    List<ResearchProfile> getAllResearchInfo();

    void setResearchProfile(ResearchProfile user);

    default Double getCurrentFPSValue(){
        return getAllResearchInfo().stream()
                .map(ResearchProfile::getFps)
                .filter(fps -> fps != null && fps > 0)
                .findFirst()
                .map(Double::valueOf)
                .orElse(DEFAULT_FPS);
    }

}
