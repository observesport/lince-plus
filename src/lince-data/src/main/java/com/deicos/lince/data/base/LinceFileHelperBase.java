package com.deicos.lince.data.base;

import com.deicos.lince.data.LinceDataConstants;
import com.deicos.lince.data.bean.categories.Category;
import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.data.bean.wrapper.LinceFileProjectWrapper;
import com.deicos.lince.data.util.JavaFXLogHelper;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * lince-scientific-base
 * com.deicos.lince
 * @author berto (alberto.soto@gmail.com)in 24/02/2017.
 * Description:
 * <p>
 * Al hacer referencia a la applicacion javafx, tiene que estar en contexto server
 */
public class LinceFileHelperBase {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * gets filepath from user preferences
     *
     * @return lastSavedFile
     */
    public File getLinceProjectFilePath(Class baseClass) {
        Preferences prefs = Preferences.userNodeForPackage(baseClass.getClass());
        String filePath = prefs.get(LinceDataConstants.PREFERENCES_FILE_PATH, null);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    /**
     * Sets the file path of the currently loaded file. The path is persisted in
     * the OS specific registry.
     *
     * @param file the file or null to remove the path
     */
    public <T extends ILinceApp> void setLinceProjectPath(File file, T myLinceApp) {
        Preferences prefs = Preferences.userNodeForPackage(myLinceApp.getClass());
        Stage primaryStage = myLinceApp.getPrimaryStage();
        if (file != null) {
            prefs.put(LinceDataConstants.PREFERENCES_FILE_PATH, file.getPath());
            // Update the stage title.
            primaryStage.setTitle(myLinceApp.getWindowTitle() + " - " + file.getName());
        } else {
            prefs.remove(LinceDataConstants.PREFERENCES_FILE_PATH);
            // Update the stage title.
            primaryStage.setTitle(myLinceApp.getWindowTitle());
        }
    }


    private JAXBContext getXMLContext() throws JAXBException {
        //LinceFileProjectWrapper.class, Criteria.class, Category.class, ResearchProfile.class
        return JAXBContext.newInstance(LinceFileProjectWrapper.class, Criteria.class, Category.class);
    }
    /**
     * 2019 - Functional function - DARK ZONE
     * Unmarshalls file, and executes desired code with it
     *
     * @param file Loaded file
     * @param f    Function to execute
     * @return Unmarshalled info
     */
    private LinceFileProjectWrapper readProjectFile(File file, Consumer<LinceFileProjectWrapper> f) {
        try {
            JAXBContext context = getXMLContext();
            Unmarshaller um = context.createUnmarshaller();
            // Reading XML from the file and unmarshalling.
            LinceFileProjectWrapper data = (LinceFileProjectWrapper) um.unmarshal(file);
            f.accept(data);
            return data;
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
            // ASF: Fully functional version
            readProjectFile(file, linceFileProjectWrapper -> {
                myLinceApp.getDataHubService().getCriteria().setAll(linceFileProjectWrapper.getCriteriaData());
                myLinceApp.getDataHubService().getVideoPlayList().setAll(linceFileProjectWrapper.getVideoPlayList());
                myLinceApp.getDataHubService().getUserData().setAll(linceFileProjectWrapper.getProfiles());
                myLinceApp.getDataHubService().getDataRegister().setAll(linceFileProjectWrapper.getRegister());
            });
            boolean isEmptyApp = myLinceApp instanceof EmptyLinceApp;
            if (!isEmptyApp){
                // Save the file path to the registry.
                setLinceProjectPath(file, myLinceApp);
                JavaFXLogHelper.addLogInfo("Cargado fichero " + file.getName());
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
            JAXBContext context = getXMLContext();
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            // Wrapping our person data.
            LinceFileProjectWrapper wrapper = new LinceFileProjectWrapper();
            wrapper.setProfiles(myLinceApp.getDataHubService().getUserData());
            wrapper.setCriteriaData(myLinceApp.getDataHubService().getCriteria());
            wrapper.setVideoPlayList(myLinceApp.getDataHubService().getVideoPlayList());
            wrapper.setRegister(myLinceApp.getDataHubService().getDataRegister());
            // Marshalling and saving XML to the file.
            m.marshal(wrapper, file);
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
                myLinceApp.getDataHubService().getDataRegister().addAll(projectWrapper.getRegister());
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
                myLinceApp.getDataHubService().getCriteria().setAll(projectWrapper.getCriteriaData());
                myLinceApp.getDataHubService().getDataRegister().setAll(projectWrapper.getRegister());
            });
        } catch (Exception e) {
            JavaFXLogHelper.showMessage(Alert.AlertType.ERROR
                    , "Could not load data"
                    , "addBaseProjectInfo - Could not load data from file:\n" + file.getPath());
        }
    }
}
