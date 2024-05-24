package com.lince.observer.data.base;

import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.util.LinceFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * lince-scientific-base
 *
 * @author berto (alberto.soto@gmail.com)in 24/02/2017.
 * Description:
 * <p>
 * Al hacer referencia a la applicacion javafx, tiene que estar en contexto server
 */
public class LinceFileHelperFx extends LinceFileHelper {


    private Preferences getPreferences() {
        return Preferences.userNodeForPackage(ILinceApp.class);
    }

    /**
     * gets filepath from user preferences
     * Returns system user file path by default
     *
     * @return lastSavedFile, user path or temp path
     */
    public File getLinceProjectFilePath() {
        try {
            Preferences prefs = getPreferences();
            String filePath = prefs.get(LinceDataConstants.PREFERENCES_FILE_PATH, null);
            if (filePath != null) {
                return new File(filePath);
            } else {
                return FileUtils.getUserDirectory();
            }
        } catch (Exception e) {
            log.error("file not found", e);
            return FileUtils.getTempDirectory();
        }
    }

    /**
     * Gets temporary file in same path that project file with a "_TMP" sufix
     *
     * @return Temp file
     */
    public File getLinceTmpProjectFile() {
        try {
            File file = getLinceProjectFilePath();
            if (!file.isDirectory()) {
          /*      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dateFormat.format(System.currentTimeMillis());*/
                File tmpFile = new File(file.getParentFile(), StringUtils.replace(file.getName(), ".xml", /*"_" + date +*/ "_AUTOSAVE.xml"));
                tmpFile.createNewFile();
                return tmpFile;
            }
        } catch (Exception e) {
            log.error("tmp file not found", e);
        }
        return null;
    }



    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     *
     * @param file the file or null to remove the path
     */
    public <T extends ILinceApp> void setLinceProjectPath(File file, T myLinceApp) {
        Preferences prefs = getPreferences();
        Stage primaryStage = myLinceApp.getPrimaryStage();
        String titleAddon = StringUtils.EMPTY;
        if (file != null && file.exists()) {
            prefs.put(LinceDataConstants.PREFERENCES_FILE_PATH, file.getPath());
            titleAddon = " - " + file.getName();
        } else {
            prefs.remove(LinceDataConstants.PREFERENCES_FILE_PATH);
        }
        // Update the stage title.
        primaryStage.setTitle(myLinceApp.getWindowTitle() + titleAddon);
    }


    /**
     * 2019 - Functional function - DARK ZONE
     * Unmarshalls file, and executes desired code with it
     *
     * @param file Loaded file
     * @param f    Function to execute
     * @return Unmarshalled info
     */
    @Override
    protected LinceFileProjectWrapper readProjectFile(File file, Consumer<LinceFileProjectWrapper> f) {
        try {
            return super.readProjectFile(file, f);
        } catch (Exception e) { // catches ANY exception
            JavaFXLogHelper.showMessage(Alert.AlertType.ERROR
                    , "Could not load data"
                    , "Could not load data from file:\n" + file.getPath());
            return null;
        }
    }

    /**
     * Loads current file and sets it in datahubService
     *
     * @param file       XML File with data
     * @param myLinceApp currentMainApp with Spring context for reaching services
     */
    public void loadLinceProjectFromFile(File file, ILinceApp myLinceApp) {
        try {
            readProjectFile(file, linceFileProjectWrapper -> {
                myLinceApp.getDataHubService().clearData();
                myLinceApp.getDataHubService().getCriteria().setAll(linceFileProjectWrapper.getObservationTool());
                myLinceApp.getDataHubService().getVideoPlayList().setAll(linceFileProjectWrapper.getVideoPlayList());
                myLinceApp.getDataHubService().getUserData().setAll(linceFileProjectWrapper.getProfiles());
                myLinceApp.getDataHubService().setAllDataRegister(linceFileProjectWrapper.getRegister()); //ojo! setAll es clear + addAll
                myLinceApp.getDataHubService().getYoutubeVideoPlayList().setAll(linceFileProjectWrapper.getYoutubeVideoPlayList());
                myLinceApp.getDataHubService().updateUserPlayList();
            });
            boolean isEmptyApp = myLinceApp instanceof EmptyLinceApp;
            if (!isEmptyApp && !file.isDirectory()) {
                // Save the file path to the registry.
                setLinceProjectPath(file, myLinceApp);
                JavaFXLogHelper.addLogInfo("Cargado fichero " + file.getName());
            } else {
                JavaFXLogHelper.addLogInfo("No se ha cargado ningun fichero");
            }
        } catch (Exception e) {
            JavaFXLogHelper.showMessage(Alert.AlertType.ERROR
                    , "Could not load data"
                    , "loadLinceProjectFromFile - Could not load data from file:\n" + file.getPath());
        }
    }



    /**
     * Marshall data inside selected file
     *
     * @param file       XML File to save data
     * @param myLinceApp currentMainApp with Spring context for reaching services
     */
    public void saveLinceProjectToFile(File file, ILinceApp myLinceApp) {
        try {
            Date now = new Date(System.currentTimeMillis());
            myLinceApp.getDataHubService().getUserData().forEach(researchProfile -> researchProfile.setSaveDate(now));
            file = saveFile(file
                    , myLinceApp.getDataHubService().getUserData()
                    , myLinceApp.getDataHubService().getCriteria()
                    , myLinceApp.getDataHubService().getVideoPlayList()
                    , myLinceApp.getDataHubService().getYoutubeVideoPlayList()
                    , myLinceApp.getDataHubService().getDataRegister());
            // Save the file path to the registry.
            setLinceProjectPath(file, myLinceApp);
            JavaFXLogHelper.addLogInfo("Guardado fichero " + file.getName());
        } catch (Exception e) { // catches ANY exception
            log.error("Error saving file", e);
            JavaFXLogHelper.showMessage(Alert.AlertType.ERROR
                    , "Could not save data"
                    , "Could not save data to file:\n" + file.getPath());
        }
    }


    public void addObservations(File file, ILinceApp myLinceApp) {
        try {
            readProjectFile(file, projectWrapper -> {
                List<LinceRegisterWrapper> newItems = projectWrapper.getRegister();
                myLinceApp.getDataHubService().addDataRegister(newItems);
                //myLinceApp.getDataHubService().getDataRegister().addAll(newItems);
            });
        } catch (Exception e) {
            JavaFXLogHelper.showMessage(Alert.AlertType.ERROR
                    , "Could not load data"
                    , "addObservations - Could not load data from file:\n" + file.getPath());
        }
    }

    public void addBaseProjectInfo(File file, ILinceApp myLinceApp) {
        try {
            readProjectFile(file, projectWrapper -> {
                myLinceApp.getDataHubService().getCriteria().setAll(projectWrapper.getObservationTool());
                myLinceApp.getDataHubService().setAllDataRegister(projectWrapper.getRegister());
            });
        } catch (Exception e) {
            JavaFXLogHelper.showMessage(Alert.AlertType.ERROR
                    , "Could not load data"
                    , "addBaseProjectInfo - Could not load data from file:\n" + file.getPath());
        }
    }
}
