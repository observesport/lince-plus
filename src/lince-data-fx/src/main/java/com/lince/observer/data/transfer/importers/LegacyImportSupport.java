package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.TransferException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared plumbing of the legacy importers (LO-349): spooling the upload to a temp file for the legacy loaders
 * that need a {@link File}, adding an imported observer to the target project, and building the outcome.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
final class LegacyImportSupport {

    private LegacyImportSupport() {
    }

    /**
     * @param in     upload content, not closed here
     * @param suffix temp file suffix, with the dot
     * @return a temp file holding the content; the caller deletes it with {@code FileUtils.deleteQuietly}
     * @throws ImportException IO when the temp file cannot be written; the partial temp file is deleted first
     */
    static File spool(InputStream in, String suffix) {
        Path tmp = null;
        try {
            tmp = Files.createTempFile("lince-import-", suffix);
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            return tmp.toFile();
        } catch (IOException e) {
            FileUtils.deleteQuietly(tmp == null ? null : tmp.toFile());
            throw new ImportException(TransferException.Reason.IO, "Cannot buffer the uploaded file", e);
        }
    }

    /**
     * Adds the rows as a new observer, as {@code DataHubServiceBase.addDataRegister} + {@code addObserver} do for
     * the live project: new register id, new user profile whose register code is that id, and the profile added
     * to the first research profile.
     *
     * @param target       project receiving the observer
     * @param observerName user name of the new observer
     * @param rows         imported observations
     * @return the new register
     */
    static LinceRegisterWrapper addObserver(ILinceProject target, String observerName, List<RegisterItem> rows) {
        LinceRegisterWrapper register = new LinceRegisterWrapper();
        register.setRegisterData(new ArrayList<>(rows));
        UserProfile profile = new UserProfile();
        profile.setUserName(observerName);
        profile.setKey(UUID.randomUUID());
        profile.setRegisterCode(register.getId());
        register.setUserProfile(profile);
        List<LinceRegisterWrapper> registers = target.getRegister() == null
                ? new ArrayList<>() : new ArrayList<>(target.getRegister());
        registers.add(register);
        target.setRegister(registers);
        List<ResearchProfile> researches = target.getProfiles();
        if (researches != null && !researches.isEmpty()) {
            ResearchProfile research = researches.get(0);
            List<UserProfile> users = research.getUserProfiles() == null
                    ? new ArrayList<>() : new ArrayList<>(research.getUserProfiles());
            users.add(profile);
            research.setUserProfiles(users);
        }
        return register;
    }

    /**
     * @param target       project after the import
     * @param observations observations imported
     * @param warnings     non fatal issues
     * @return counts of the target project
     */
    static ImportOutcome outcome(ILinceProject target, int observations, List<String> warnings) {
        return new ImportOutcome(size(target.getObservationTool()), size(target.getRegister()), observations,
                size(target.getProfiles()), warnings);
    }

    static boolean hasObservations(ILinceProject target) {
        return target.getRegister() != null && target.getRegister().stream()
                .anyMatch(r -> r.getRegisterData() != null && !r.getRegisterData().isEmpty());
    }

    private static int size(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
