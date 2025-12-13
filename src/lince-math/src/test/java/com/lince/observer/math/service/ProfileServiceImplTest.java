package com.lince.observer.math.service;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.component.LinceFileImporter;
import com.lince.observer.data.service.ProfileService;
import com.lince.observer.math.LinceServiceTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProfileServiceImpl
 * Tests the observer deletion functionality
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LinceServiceTestConfig.class)
class ProfileServiceImplTest {

    private final Logger logger = LoggerFactory.getLogger(ProfileServiceImplTest.class);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private DataHubService dataHubService;

    @BeforeEach
    void setUp() {
        // Load a project with multiple observers
        ILinceProject project = loadMultipleObserverProject();
        dataHubService.setLinceProject(project);
    }

    private ILinceProject loadMultipleObserverProject() {
        LinceFileImporter linceFileImporter = new LinceFileImporter();
        return linceFileImporter.importLinceProject(
                new File(Objects.requireNonNull(getClass().getClassLoader()
                        .getResource("multipleObserverExample.xml")).getFile()));
    }

    @Test
    void testRemoveObserverByUUID() {
        // Arrange
        List<LinceRegisterWrapper> registers = dataHubService.getDataRegister();
        int initialCount = registers.size();

        logger.info("Initial observer count: {}", initialCount);
        assertTrue(initialCount >= 2, "Test requires at least 2 observers");

        // Get the first observer's UUID to keep
        UUID firstObserverUUID = registers.get(0).getUserProfile().getKey();

        // Get the second observer's UUID to remove
        UUID secondObserverUUID = registers.get(1).getUserProfile().getKey();

        logger.info("Keeping observer with UUID: {}", firstObserverUUID);
        logger.info("Removing observer with UUID: {}", secondObserverUUID);

        // Create a ResearchProfile with only the first observer
        ResearchProfile profile = new ResearchProfile();
        List<UserProfile> userProfiles = new ArrayList<>();
        userProfiles.add(registers.get(0).getUserProfile());
        profile.setUserProfiles(userProfiles);

        // Act
        profileService.setResearchProfile(profile);

        // Assert
        List<LinceRegisterWrapper> updatedRegisters = dataHubService.getDataRegister();
        int finalCount = updatedRegisters.size();

        logger.info("Final observer count: {}", finalCount);
        assertEquals(1, finalCount, "Should have only 1 observer after deletion");

        // Verify the removed observer is not in the list
        boolean secondObserverExists = updatedRegisters.stream()
                .anyMatch(reg -> reg.getUserProfile().getKey().equals(secondObserverUUID));

        assertFalse(secondObserverExists, "Second observer should have been removed");

        // Verify the kept observer is still in the list
        boolean firstObserverExists = updatedRegisters.stream()
                .anyMatch(reg -> reg.getUserProfile().getKey().equals(firstObserverUUID));

        assertTrue(firstObserverExists, "First observer should still exist");
    }

    @Test
    void testRemoveMultipleObserversByUUID() {
        // Arrange
        List<LinceRegisterWrapper> registers = dataHubService.getDataRegister();
        int initialCount = registers.size();

        logger.info("Initial observer count: {}", initialCount);
        assertTrue(initialCount >= 2, "Test requires at least 2 observers");

        // Keep only the first observer, remove all others
        UUID keepUUID = registers.get(0).getUserProfile().getKey();
        List<UUID> removeUUIDs = registers.stream()
                .skip(1)
                .map(reg -> reg.getUserProfile().getKey())
                .collect(Collectors.toList());

        logger.info("Keeping observer with UUID: {}", keepUUID);
        logger.info("Removing {} observers", removeUUIDs.size());

        // Create a ResearchProfile with only the first observer
        ResearchProfile profile = new ResearchProfile();
        List<UserProfile> userProfiles = new ArrayList<>();
        userProfiles.add(registers.get(0).getUserProfile());
        profile.setUserProfiles(userProfiles);

        // Act
        profileService.setResearchProfile(profile);

        // Assert
        List<LinceRegisterWrapper> updatedRegisters = dataHubService.getDataRegister();
        int finalCount = updatedRegisters.size();

        logger.info("Final observer count: {}", finalCount);
        assertEquals(1, finalCount, "Should have only 1 observer after removing all but one");

        // Verify all removed observers are not in the list
        for (UUID removedUUID : removeUUIDs) {
            boolean observerExists = updatedRegisters.stream()
                    .anyMatch(reg -> reg.getUserProfile().getKey().equals(removedUUID));
            assertFalse(observerExists,
                    String.format("Observer %s should have been removed", removedUUID));
        }

        // Verify the kept observer is still in the list
        boolean keptObserverExists = updatedRegisters.stream()
                .anyMatch(reg -> reg.getUserProfile().getKey().equals(keepUUID));
        assertTrue(keptObserverExists, "Kept observer should still exist");
    }

    @Test
    void testAddNewObserver() {
        // Arrange
        List<LinceRegisterWrapper> registers = dataHubService.getDataRegister();
        int initialCount = registers.size();

        logger.info("Initial observer count: {}", initialCount);

        // Create a new UserProfile
        UserProfile newUser = new UserProfile();
        newUser.setKey(UUID.randomUUID());
        newUser.setUserName("New Test Observer");

        // Create a ResearchProfile with all existing observers plus the new one
        ResearchProfile profile = new ResearchProfile();
        List<UserProfile> userProfiles = registers.stream()
                .map(LinceRegisterWrapper::getUserProfile)
                .collect(Collectors.toList());
        userProfiles.add(newUser);
        profile.setUserProfiles(userProfiles);

        // Act
        profileService.setResearchProfile(profile);

        // Assert
        List<LinceRegisterWrapper> updatedRegisters = dataHubService.getDataRegister();
        int finalCount = updatedRegisters.size();

        logger.info("Final observer count: {}", finalCount);
        assertEquals(initialCount + 1, finalCount,
                "Should have one more observer after addition");

        // Verify the new observer exists in the list
        boolean newObserverExists = updatedRegisters.stream()
                .anyMatch(reg -> reg.getUserProfile().getKey().equals(newUser.getKey()));
        assertTrue(newObserverExists, "New observer should have been added");
    }
}