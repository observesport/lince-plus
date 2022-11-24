package com.deicos.lince.app.javafx.view;

import com.deicos.lince.app.helper.ServerValuesHelper;
import com.deicos.lince.app.javafx.JavaFXLoader;
import com.deicos.lince.app.javafx.components.JavaFXBrowser;
import com.deicos.lince.app.javafx.generic.JavaFXLinceBaseController;
import com.deicos.lince.data.LinceDataConstants;
import com.deicos.lince.data.bean.user.ResearchProfile;
import com.deicos.lince.data.bean.user.UserProfile;
import com.deicos.lince.data.export.Lince2ThemeExport;
import com.deicos.lince.data.legacy.Command;
import com.deicos.lince.data.legacy.commands.importar.AbrirImportarHoisan;
import com.deicos.lince.data.legacy.commands.instrumento.LoadInstrumentoObservacional;
import com.deicos.lince.data.legacy.commands.instrumento.SaveInstrumentoObservacionalAs;
import com.deicos.lince.data.legacy.commands.registro.LoadRegistro;
import com.deicos.lince.data.legacy.commands.registro.SaveRegistroAs;
import com.deicos.lince.data.legacy.exportar.*;
import com.deicos.lince.data.system.operations.LinceFileHelper;
import com.deicos.lince.data.util.JavaFXLogHelper;
import com.deicos.lince.math.service.DataHubService;
import com.deicos.lince.math.service.LegacyConverterService;
import com.deicos.lince.transcoding.component.TranscodingProvider;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lince.modelo.InstrumentoObservacional.InstrumentoObservacional;
import lince.modelo.Registro;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.function.Consumer;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 *
 * @author Alberto Soto Fernandez
 */
@Component
public class RootLayoutController extends JavaFXLinceBaseController {

    @Autowired
    protected DataHubService dataHubService;

    protected TranscodingProvider transcodingProvider = null;

    @FXML
    private ListView logArea;

    @FXML
    private ListView videoPlaylistView;

    @FXML
    private Pane pane;


    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        log.info("--            Init RootLayoutController               --");
        logArea.setItems(JavaFXLogHelper.getFxLog());
        //htmlContext.setPromptText("PUM");
    }

    public void lazyInit() {
        try {
            getMainLinceApp().getPrimaryStage().setOnCloseRequest(confirmCloseEventHandler);
            //es asi pero debe ser despues!
            dataHubService = getMainLinceApp().getDataHubService();
            videoPlaylistView.setItems(dataHubService.getVideoPlayList());
            videoPlaylistView.setCellFactory(lv -> {
                ListCell<File> cell = new ListCell<File>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getAbsolutePath());
                        } else {
                            setText(StringUtils.EMPTY);
                        }
                    }
                };
                ContextMenu contextMenu = new ContextMenu();
                MenuItem editItem = new MenuItem();
                editItem.textProperty().bind(Bindings.format("Eliminar de la lista"));
                editItem.setOnAction(event -> {
                    //log.info("a tomar por culo el fichero", item.getName());
                    File item = cell.getItem();
                    for (File file : dataHubService.getVideoPlayList()) {
                        if (item.equals(file)) {
                            dataHubService.getVideoPlayList().remove(file);
                            //  videoPlaylistView.setItems(dataHubService.getVideoPlayList());
                            JavaFXLogHelper.addLogInfo("Eliminado del proyecto el video " + file.getName());
                        }
                    }
                });
                contextMenu.getItems().addAll(editItem);
                cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) ->
                        cell.setContextMenu(isNowEmpty ? null : contextMenu)
                );
                return cell;
            });
            JavaFXBrowser browser = new JavaFXBrowser(getMainLinceApp().getServerURL() + "/desktop.html");
            pane.getChildren().add(browser);

        } catch (Exception e) {
            log.error("Al inicializar lista de ficheros");
        }
    }


    /**
     * Creates an empty address book.
     */
    @FXML
    private void handleNew() {
        dataHubService.clearData();
        mainLinceApp.getAnalysisService().getDataRegister().clear();
        mainLinceApp.getCategoryService().clearAll();
        Registro.loadNewInstance();//legacy
        LinceFileHelper fileHelper = new LinceFileHelper();
        fileHelper.setLinceProjectPath(null, mainLinceApp);
        JavaFXLogHelper.addLogInfo("Nuevo proyecto");
    }

    /**
     * Functional Code for opening a lince project
     *
     * @param f
     */
    private void doOpenLincePlusProject(Consumer<File> f) {
        try {
            LinceFileHelper fileHelper = new LinceFileHelper();
            FileChooser fileChooser = fileHelper.getFileChooser();

            // Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            // Show save file dialog
            File file = fileChooser.showOpenDialog(mainLinceApp.getPrimaryStage());
            if (file != null) {
                f.accept(file);
            }
        } catch (Exception e) {
            JavaFXLogHelper.addLogError("Error opening lince Project", e);
        }
    }

    /**
     * Opens a FileChooser to let the user select a project file.
     */
    @FXML
    private void handleOpen() {
        doOpenLincePlusProject(file -> {
                    LinceFileHelper fileHelper = new LinceFileHelper();
                    fileHelper.loadLinceProjectFromFile(file, mainLinceApp);
                }
        );
    }

    /**
     * Opens a FileChooser to let the user select a project file
     * Imports instrument, observation and profiles.
     * Does not import video
     */
    @FXML
    private void handleImportLincePlusProject() {
        doOpenLincePlusProject(file -> {
                    LinceFileHelper fileHelper = new LinceFileHelper();
                    fileHelper.addBaseProjectInfo(file, mainLinceApp);
                    dataHubService.reloadResearchProfileDataFromRegister();
                    JavaFXLogHelper.addLogInfo("Se ha cargado el instrumento y la observación del proyecto seleccionado");
                }
        );
    }


    /**
     * Opens a FileChooser to let the user select a project file
     * It only adds all observations into current project
     */
    @FXML
    private void handleImportLincePlusObserver() {
        doOpenLincePlusProject(file -> {
                    LinceFileHelper fileHelper = new LinceFileHelper();
                    fileHelper.addObservations(file, mainLinceApp);
                    JavaFXLogHelper.addLogInfo("Se han importado los datos de observación del proyecto seleccionado");
                }
        );
    }

    /**
     * Saves the file to the person file that is currently open. If there is no
     * open file, the "save as" dialog is shown.
     */
    @FXML
    private void handleSave() {
        LinceFileHelper fileHelper = new LinceFileHelper();
        File file = fileHelper.getLinceProjectFilePath();
        if (file != null) {
            fileHelper.saveLinceProjectToFile(file, mainLinceApp);
        } else {
            handleSaveAs();
        }
    }

    /**
     * Opens a FileChooser to let the user select a file to save to.
     */
    @FXML
    private void handleSaveAs() {
        LinceFileHelper fileHelper = new LinceFileHelper();
        FileChooser fileChooser = fileHelper.getFileChooser();
        // Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        // Show save file dialog
        File file = fileChooser.showSaveDialog(mainLinceApp.getPrimaryStage());
        if (file != null) {
            // Make sure it has the correct extension
            if (!file.getPath().endsWith(".xml")) {
                file = new File(file.getPath() + ".xml");
            }
            fileHelper.saveLinceProjectToFile(file, mainLinceApp);
        }
    }

    @FXML
    private void handleSelectVideo() {
        if (this.transcodingProvider == null) {
            this.transcodingProvider = getMainLinceApp().getTranscodingProvider();
        }
        final String label = "Video ";
        List<Pair<String, String>> supportedTypes = new ArrayList<>();
        for (String type : StringUtils.split(LinceDataConstants.SUPPORTED_VIDEO_FILES, ";")) {
            supportedTypes.add(new MutablePair<>(label + type, type));
        }
        List<File> fileList = LinceFileHelper.openMultipleFileDialog(mainLinceApp, supportedTypes);
        if (CollectionUtils.isNotEmpty(fileList)) {
            StringBuilder urls = new StringBuilder();
            boolean conversionDone = false;
            for (File file : fileList) {
                urls.append("- ").append(file.getPath()).append("\n");
                File file2Add = transcodingProvider.reviewVideoFile(file);
                if (file2Add != file) {
                    conversionDone = true;
                }
                dataHubService.getVideoPlayList().add(file2Add);
            }
            JavaFXLogHelper.showMessage(AlertType.INFORMATION
                    , "Videos añadido"
                    , urls.toString());
            if (conversionDone) {
                JavaFXLogHelper.showMessage(AlertType.INFORMATION
                        , "Conversión realizada"
                        , "Se han generado automáticamente ficheros MP4 para los videos seleccionados en la misma carpeta del original");
            }
        } else {
            if (CollectionUtils.isEmpty(dataHubService.getVideoPlayList())) {
                JavaFXLogHelper.showMessage(AlertType.ERROR
                        , getMainLinceApp().getMessage("label_add_item", "video")
                        , getMainLinceApp().getMessage("data_min", "video", "realizar el estudio"));
            } else {
                JavaFXLogHelper.showMessage(AlertType.INFORMATION
                        , getMainLinceApp().getMessage("label_select_item", "videos")
                        , "No se ha añadido el video aunque tienes alguno en la playlist");
            }
        }
        JavaFXLogHelper.addLogInfo("Seleccion de videos");
    }

    @FXML
    private void handleClearVideos() {
        dataHubService.getVideoPlayList().clear();
        JavaFXLogHelper.showMessage(AlertType.INFORMATION
                , getMainLinceApp().getMessage("data_list", "Videos")
                , getMainLinceApp().getMessage("data_deleted", "todos los videos"));
        JavaFXLogHelper.addLogInfo("Borrados todos los videos");
    }

    /**
     * Opens an about dialog.
     */
    @FXML
    private void handleAbout() {
        JavaFXLogHelper.showMessage(AlertType.INFORMATION
                , getMainLinceApp().getMessage("about")
                , getMainLinceApp().getMessage("about_content"));
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        JavaFXLoader.exit();
    }


    @FXML
    private void handleOpenBrowser() {
        String url = mainLinceApp.getServerURL();
        JavaFXLogHelper.addLogInfo("Abriendo navegador para analisis ubicado en " + url);
        ServerValuesHelper.openLANLinceBrowser(url, false);
    }

    /**
     * Common method on exports and imports to legacy features
     */

    private void ensureCompatibility(boolean isExport) {
        ensureCompatibility(isExport, false);
    }

    private void ensureCompatibility(boolean isExport, boolean checkMultipleObservers) {
        try {
            Registro.getInstance();
            InstrumentoObservacional.getInstance();
            LegacyConverterService converter = getMainLinceApp().getLegacyConverterService();
            if (isExport) {
                UUID uuid = null;
                if (checkMultipleObservers) {
                    uuid = getResearchSelection();
                }
                converter.migrateDataToLegacy(uuid);
            } else {
                converter.migrateDataFromLegacy();
            }
        } catch (Exception e) {
            JavaFXLogHelper.addLogError("Compatibility issue", e);
        }
    }


    /**
     * Muestra un panel para seleccionar el observador si existe más de uno en el proyecto.
     * En caso contrario devuelve null
     *
     * @return UUID de observador seleccionado o nulo por defecto
     */
    private UUID getResearchSelection() {
        try {
            List<ResearchProfile> researchers = getMainLinceApp().getProfileService().getAllResearchInfo();
            if (researchers.isEmpty()) {
                return null;
            } else {
                Map<UUID, ButtonType> uuids = new HashMap<>();
                for (ResearchProfile user : researchers) {
                    for (UserProfile researcher : user.getUserProfiles()) {
                        uuids.put(researcher.getRegisterCode(), new ButtonType(StringUtils.defaultIfEmpty(researcher.getUserName(), "Sin definir")));
                    }
                }
                if (uuids.size() < 2) {
                    return null;
                }
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Selección de observador");
                alert.setHeaderText("Para continuar, debes seleccionar un observador");
                alert.setContentText("¿Qué observador quieres seleccionar?");
                ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                uuids.put(null, buttonTypeCancel);
                alert.getButtonTypes().setAll(uuids.values());
                Optional<ButtonType> result = alert.showAndWait();
                String choice = result.get().getText();
                if (StringUtils.isNotEmpty(choice) && !StringUtils.equals(choice, "Cancel")) {
                    for (Map.Entry<UUID, ButtonType> entry : uuids.entrySet()) {
                        if (StringUtils.equals(choice, entry.getValue().getText())) {
                            return entry.getKey();
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Data integration to Export issues
     *
     * @param cmd   Legacy command
     * @param panel Legacy panel
     * @param key   i18n id
     * @param label i18n adddon
     */
    private void doExport(Command cmd, JPanel panel, String key, String label) {
        doDataIntegration(cmd, panel, key, label, true);
    }

    /**
     * data Integration to Import issues
     *
     * @param cmd   Legacy command
     * @param panel Legacy panel
     * @param key   i18n id
     * @param label i18n adddon
     */
    private void doImport(Command cmd, JPanel panel, String key, String label) {
        doDataIntegration(cmd, panel, key, label, false);
    }

    /**
     * To avoid multiple same lines. Integrates all data encapsulatino to legacy ways
     *
     * @param cmd      Legacy command
     * @param panel    Legacy panel
     * @param key      i18n id
     * @param label    i18n adddon
     * @param isExport is Export or import
     */
    private void doDataIntegration(Command cmd, JPanel panel, String key, String label, boolean isExport) {
        String i18n = getMainLinceApp().getMessage(key, label);
        try {
            Registro.loadNewInstance();
            ensureCompatibility(isExport, isExport); //pasamos registro seleccionado
            if (cmd != null) {
                cmd.execute();
            } else {
                openEmbedded(panel, i18n);
            }
            if (!isExport) {
                //solo es el caso de Hoisan y de ficheros de carga
                ensureCompatibility(false);
            }
            JavaFXLogHelper.addLogInfo(i18n);
        } catch (Exception e) {
            JavaFXLogHelper.addLogError(i18n, e);
        }
    }

    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleImportRegisterLince1() {
        ensureCompatibility(true);//para importar tenemos que migrar el instrumento!
        doImport(new LoadRegistro(), null, "panel_import_custom", "Registros de la versión 1 de Lince");
    }

    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleImportToolLince1() {
        doImport(new LoadInstrumentoObservacional(), null, "panel_import_custom", "Instrumento observacional de la versión 1 de Lince");
    }


    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleExportRegisterLince1() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(new SaveRegistroAs(), null, "panel_export_custom", "Registros para version 1 de Lince");
    }

    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleExportToolLince1() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(new SaveInstrumentoObservacionalAs(), null, "panel_export_custom", "Instrumento observacional para version 1 de Lince");
    }

    /**
     * HOISAN compatibility
     */
    @FXML
    private void handleImportHoisan() {
        doImport(new AbrirImportarHoisan(), null, "panel_import_custom", "Hoisan");
    }

    /**
     * HOISAN compatibility
     */
    @FXML
    private void handleExportHoisan() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(new AbrirExportarHoisan(), null, "panel_export_custom", "Hoisan");
    }

    /***
     * Valid export to theme 6.
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportTheme6() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(null, new ExportarTheme6Panel(), "panel_export_custom", "Theme 6");
    }

    @FXML
    private void handleExportTheme6Register() {
        try {
            LinceFileHelper fileHelper = new LinceFileHelper();
            FileChooser fileChooser = fileHelper.getFileChooser();
            // Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("VTT files (*.vtt)", "*.vtt");
            fileChooser.getExtensionFilters().add(extFilter);
            // Show save file dialog
            File file = fileChooser.showSaveDialog(mainLinceApp.getPrimaryStage());
            Lince2ThemeExport tsv = new Lince2ThemeExport(mainLinceApp.getDataHubService().getCurrentDataRegister());
            if (file != null) {
                // Make sure it has the correct extension
                if (!file.getPath().endsWith(".vtt")) {
                    file = new File(file.getPath() + ".vtt");
                }
                FileWriter fileWriter = new FileWriter(file);
                tsv.createFile(fileWriter);
            }
            JavaFXLogHelper.addLogInfo("Exporting Theme file was ok");
        } catch (Exception e) {
            JavaFXLogHelper.addLogError("Exporting Theme file content", e);
        }
    }

    /***
     * Valid export to theme 5.
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportTheme5() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(null, new ExportarTheme5Panel(), "panel_export_custom", "Theme 5");
    }

    /***
     * Valid export to SAS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSAS() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(null, new ExportarSasPanel(), "panel_export_custom", "SAS");
    }

    /***
     * Valid export to excel with csv file
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportExcel() {
        ensureCompatibility(true);//pasamos instrumento
        doExport(null, new ExportarCsvPanel(), "panel_export_custom", "Excel");
    }

    /***
     * Valid export to SDIS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQEstado() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(null, new ExportarSdisGseqEstadoPanel(), "panel_export_custom", "SDIS Seq por estados");
    }

    /***
     * Valid export to SDIS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQEvent() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(null, new ExportarSdisGseqEventoPanel(), "panel_export_custom", "SDIS Seq por eventos");
    }

    /***
     * Valid export to SDIS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQTimeEvent() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(null, new ExportarSdisGseqEventoConTiempoPanel(), "panel_export_custom", "SDIS Seq por eventos y tiempo");
    }

    /***
     * Valid export to SDIS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQInterval() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(null, new ExportarSdisGseqIntervaloPanel(), "panel_export_custom", "SDIS Seq por intérvalos");
    }

    /***
     * Valid export to SDIS.
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQMultiEvent() {
        ensureCompatibility(true);//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(null, new ExportarSdisGseqMultieventoPanel(), "panel_export_custom", "SDIS Seq por eventos múltiples");
    }

    private EventHandler<WindowEvent> confirmCloseEventHandler = event -> {
        try {
            Stage primaryStage = getMainLinceApp().getPrimaryStage();
            Alert closeConfirmation = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "¿Quieres guardar antes de salir?"
            );
            Button exitButton = (Button) closeConfirmation.getDialogPane().lookupButton(
                    ButtonType.OK
            );
            exitButton.setText("Si");
            Button noButton = (Button) closeConfirmation.getDialogPane().lookupButton(
                    ButtonType.CANCEL
            );
            noButton.setText("No");
            closeConfirmation.setHeaderText("Salir");
            closeConfirmation.initModality(Modality.APPLICATION_MODAL);
            closeConfirmation.initOwner(primaryStage);
            // normally, you would just use the default alert positioning,
            // but for this simple sample the main stage is small,
            // so explicitly position the alert so that the main window can still be seen.
            closeConfirmation.setX(primaryStage.getX() + (primaryStage.getWidth() / 2 - 150));
            closeConfirmation.setY(primaryStage.getY() + (primaryStage.getHeight() / 2 - 100));
            Optional<ButtonType> closeResponse = closeConfirmation.showAndWait();
            if (!ButtonType.OK.equals(closeResponse.get())) {
                //stop();
                closeResponse.notify();
                event.consume();
            } else {
                handleSaveAs();
                //stop();
                closeResponse.notify();
                event.consume();
            }
        } catch (Exception e) {
            log.info("onClose Exception");
        }

    };
}