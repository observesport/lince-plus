package com.lince.observer.desktop.spring.service;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.service.ProfileService;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import com.lince.observer.math.service.DataHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final LegacyBridge legacyBridge;

    public CurrentProjectProvider(DataHubService dataHubService, ProfileService profileService,
                                  LegacyBridge legacyBridge) {
        this.dataHubService = dataHubService;
        this.profileService = profileService;
        this.legacyBridge = legacyBridge;
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
     * @return the loaded project over fresh lists, safe to mutate before applying it back. Registers, criteria and
     * observers ({@code UserProfile}) are the live instances; each {@link ResearchProfile} is a copy with its own
     * observer list, because importers rebuild that list ({@code LinceProjectImporter.mergeFull},
     * {@code LegacyImportSupport.addObserver}) and a failed import must leave the live one untouched
     */
    public ILinceProject snapshot() {
        ILinceProject project = new LinceFileProjectWrapper();
        project.setProfiles(copyResearchProfiles(dataHubService.getUserData()));
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

    /**
     * Replaces the legacy {@link Registro} singleton with an empty one (New project), under the
     * {@link LegacyBridge} lock so it never swaps the register in the middle of a legacy import or export.
     */
    public void resetLegacyRegister() {
        legacyBridge.withLegacyModel(() -> {
            Registro.loadNewInstance();
            return null;
        });
    }

    private static List<ResearchProfile> copyResearchProfiles(List<ResearchProfile> source) {
        return source == null ? new ArrayList<>() : source.stream().map(CurrentProjectProvider::copy).collect(
                Collectors.toCollection(ArrayList::new));
    }

    private static ResearchProfile copy(ResearchProfile source) {
        if (source == null) {
            return null;
        }
        ResearchProfile copy = new ResearchProfile();
        copy.setKey(source.getKey());
        copy.setResearchGroup(source.getResearchGroup());
        copy.setResearchName(source.getResearchName());
        copy.setResearchGroupCode(source.getResearchGroupCode());
        copy.setResearchNameCode(source.getResearchNameCode());
        copy.setFps(source.getFps());
        copy.setNumVideos(source.getNumVideos());
        copy.setSaveDate(source.getSaveDate());
        copy.setUserProfiles(copy(source.getUserProfiles()));
        return copy;
    }

    private static <T> List<T> copy(List<T> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }
}
