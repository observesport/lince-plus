package com.lince.observer.desktop.spring.service;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.service.ProfileService;
import com.lince.observer.math.service.DataHubService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentProjectProviderTest {

    private DataHubService dataHubService;
    private ProfileService profileService;
    private CurrentProjectProvider provider;

    private final ObservableList<Criteria> criteria = FXCollections.observableArrayList(new Criteria());
    private final ObservableList<ResearchProfile> profiles = FXCollections.observableArrayList(new ResearchProfile());
    private final List<LinceRegisterWrapper> registers = new ArrayList<>(List.of(new LinceRegisterWrapper()));
    private final ObservableList<File> videos = FXCollections.observableArrayList(new File("a.mp4"));
    private final ObservableList<String> youtube = FXCollections.observableArrayList("https://youtu.be/x");

    @BeforeEach
    void setUp() {
        dataHubService = mock(DataHubService.class);
        profileService = mock(ProfileService.class);
        when(dataHubService.getCriteria()).thenReturn(criteria);
        when(dataHubService.getUserData()).thenReturn(profiles);
        when(dataHubService.getDataRegister()).thenReturn(registers);
        when(dataHubService.getVideoPlayList()).thenReturn(videos);
        when(dataHubService.getYoutubeVideoPlayList()).thenReturn(youtube);
        provider = new CurrentProjectProvider(dataHubService, profileService);
    }

    @Test
    void currentExposesTheLiveCollections() {
        ILinceProject project = provider.current();

        assertSame(criteria, project.getObservationTool());
        assertSame(profiles, project.getProfiles());
        assertSame(registers, project.getRegister());
        assertSame(videos, project.getVideoPlayList());
        assertSame(youtube, project.getYoutubeVideoPlayList());
    }

    @Test
    void snapshotCopiesTheCollectionsButKeepsTheElements() {
        ILinceProject project = provider.snapshot();

        assertNotSame(criteria, project.getObservationTool());
        assertNotSame(registers, project.getRegister());
        assertNotSame(profiles, project.getProfiles());
        assertEquals(criteria, project.getObservationTool());
        assertSame(registers.get(0), project.getRegister().get(0));
        assertSame(profiles.get(0), project.getProfiles().get(0));
        assertEquals(videos, project.getVideoPlayList());
        assertEquals(youtube, project.getYoutubeVideoPlayList());

        project.getRegister().clear();
        assertEquals(1, registers.size());
    }

    @Test
    void currentResearchIdIsNullWithoutProfiles() {
        when(profileService.getAllResearchInfo()).thenReturn(List.of());

        assertNull(provider.currentResearchId());
        assertTrue(provider.userProfiles().isEmpty());
    }

    @Test
    void currentResearchIdIsTheFirstUserProfileRegisterCode() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        when(profileService.getAllResearchInfo()).thenReturn(List.of(research(first), research(second)));

        assertEquals(first, provider.currentResearchId());
        assertEquals(2, provider.userProfiles().size());
    }

    @Test
    void currentResearchIdIsNullWhenProfileServiceFails() {
        when(profileService.getAllResearchInfo()).thenThrow(new IllegalStateException("boom"));

        assertNull(provider.currentResearchId());
    }

    private static ResearchProfile research(UUID registerCode) {
        UserProfile user = new UserProfile();
        user.setRegisterCode(registerCode);
        ResearchProfile research = new ResearchProfile();
        research.setUserProfiles(new ArrayList<>(List.of(user)));
        return research;
    }
}
