package com.deicos.lince.app.component;

import com.deicos.lince.data.system.operations.LinceFileHelper;
import com.deicos.lince.data.util.JavaFXLogHelper;
import com.deicos.lince.math.service.DataHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * com.deicos.lince.app.component
 * Class ScheduledTasks
 * 05/11/2019
 * Font https://spring.io/guides/gs/scheduling-tasks/
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    static final long SAVING_WINDOW = 120000; //autosave cada 2 min
    private final DataHubService dataHubService;
    private static boolean isFirst = true;

    public ScheduledTasks(DataHubService dataHubService) {
        this.dataHubService = dataHubService;
    }

    /**
     * Creates a temp file in same directory if the user has not saved
     * or backups previous project in tmp file depending of time
     * <p>
     * When loading it checks if the
     */
    @Scheduled(fixedRate = SAVING_WINDOW)
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
}
