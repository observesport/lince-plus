package com.lince.observer.desktop.component;

import com.lince.observer.data.system.operations.LinceFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.desktop.ServerAppParams;
import com.lince.observer.math.service.DataHubService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * .app.component
 * Class ScheduledTasks
 * 05/11/2019
 * Font https://spring.io/guides/gs/scheduling-tasks/
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Component
public class ScheduledTasks {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final DataHubService dataHubService;
    private final ApplicationContextProvider applicationContextProvider;
    private static boolean isFirst = true;

    public ScheduledTasks(DataHubService dataHubService, ApplicationContextProvider applicationContextProvider) {
        this.dataHubService = dataHubService;
        this.applicationContextProvider = applicationContextProvider;
    }

    /**
     * Creates a temp file in same directory if the user has not saved
     * or backups previous project in tmp file depending of time
     * <p>
     * When loading it checks if the
     */
    @Scheduled(fixedRate = ServerAppParams.SCHEDULE_AUTOSAVE_WINDOW)
    public void doAutoSave() {
        if (!isFirst) {
            try {
                Date lastSave = dataHubService.getResearchProfile().getSaveDate();
                Date now = new Date(System.currentTimeMillis());
                LinceFileHelper helper = new LinceFileHelper();
                File file = helper.getLinceProjectFilePath();
                if (!file.isDirectory()) {
                    if (lastSave == null) {
                        lastSave = helper.getLastModifiedDate(file);
                    }
                    File tmpFile = helper.getLinceTmpProjectFile();
                    Date tmpCreationDate = helper.getLastModifiedDate(tmpFile);
                    if (lastSave.before(tmpCreationDate)) {
                        helper.saveFile(tmpFile
                                , dataHubService.getUserData()
                                , dataHubService.getCriteria()
                                , dataHubService.getVideoPlayList()
                                , dataHubService.getYoutubeVideoPlayList()
                                , dataHubService.getDataRegister());
                    }
                }
                String time = dateFormat.format(now);
                JavaFXLogHelper.addLogInfo("Guardado proyecto automaticamente a las " + time);
            } catch (Exception e) {
                JavaFXLogHelper.addLogError("No se ha podido guardar temporalmente", e);
            }
        }
        isFirst = false;
    }

    /**
     * Updates the current version of git repository
     */
    @Scheduled(fixedRate = ServerAppParams.SCHEDULE_UPDATE_CHECK_WINDOW)
    public void doUpdateCheck() {
        applicationContextProvider.setLastLinceVersion();
    }

}
