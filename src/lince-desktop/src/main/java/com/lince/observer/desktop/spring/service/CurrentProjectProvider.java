package com.lince.observer.desktop.spring.service;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.service.ProfileService;
import com.lince.observer.math.service.DataHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The project loaded in the desktop application, as an {@link ILinceProject} (LO-349).
 * Extracted from {@code RootLayoutController.getCurrentProjectDetails()} and the headless part of
 * {@code getResearchSelection()} so the REST import/export services and the JavaFX menu share it.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Service
public class CurrentProjectProvider {

    private static final Logger log = LoggerFactory.getLogger(CurrentProjectProvider.class);

    private final DataHubService dataHubService;
    private final ProfileService profileService;

    public CurrentProjectProvider(DataHubService dataHubService, ProfileService profileService) {
        this.dataHubService = dataHubService;
        this.profileService = profileService;
    }

    /**
     * @return the loaded project backed by the live collections; read it, do not mutate it
     */
    public ILinceProject current() {
        ILinceProject project = new LinceFileProjectWrapper();
        project.setProfiles(dataHubService.getUserData());
        project.setRegister(dataHubService.getDataRegister());
        project.setObservationTool(dataHubService.getCriteria());
        project.setVideoPlayList(dataHubService.getVideoPlayList());
        project.setYoutubeVideoPlayList(dataHubService.getYoutubeVideoPlayList());
        return project;
    }

    /**
     * @return the loaded project over fresh lists (same element instances), safe to mutate before applying it back
     */
    public ILinceProject snapshot() {
        ILinceProject project = new LinceFileProjectWrapper();
        project.setProfiles(copy(dataHubService.getUserData()));
        project.setRegister(copy(dataHubService.getDataRegister()));
        project.setObservationTool(copy(dataHubService.getCriteria()));
        project.setVideoPlayList(copy(dataHubService.getVideoPlayList()));
        project.setYoutubeVideoPlayList(copy(dataHubService.getYoutubeVideoPlayList()));
        return project;
    }

    /**
     * @return every observer (user profile) of every research profile, empty on failure
     */
    public List<UserProfile> userProfiles() {
        try {
            List<ResearchProfile> researchers = profileService.getAllResearchInfo();
            if (researchers == null) {
                return List.of();
            }
            return researchers.stream()
                    .filter(Objects::nonNull)
                    .map(ResearchProfile::getUserProfiles)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .toList();
        } catch (Exception e) {
            log.error("Error reading the research profiles", e);
            return List.of();
        }
    }

    /**
     * Headless observer selection: the first observer's register code, null when there is none.
     * The JavaFX menu asks the user instead when there are several observers.
     *
     * @return register code of the default observer, nullable
     */
    public UUID currentResearchId() {
        List<UserProfile> profiles = userProfiles();
        return profiles.isEmpty() ? null : profiles.get(0).getRegisterCode();
    }

    private static <T> List<T> copy(List<T> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }
}
