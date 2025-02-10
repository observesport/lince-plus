package com.lince.observer.desktop.javafx.view;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.javafx.*;
import com.lince.observer.data.legacy.Command;
import com.lince.observer.data.legacy.commands.importar.AbrirImportarHoisan;
import com.lince.observer.data.legacy.commands.instrumento.LoadInstrumentoObservacional;
import com.lince.observer.data.legacy.commands.instrumento.SaveInstrumentoObservacionalAs;
import com.lince.observer.data.legacy.commands.registro.LoadRegistro;
import com.lince.observer.data.legacy.commands.registro.SaveRegistroAs;
import com.lince.observer.data.legacy.exportar.*;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.desktop.ServerAppParams;
import com.lince.observer.desktop.helper.ServerValuesHelper;
import com.lince.observer.desktop.javafx.JavaFXLoader;
import com.lince.observer.desktop.javafx.components.JavaFXBrowser;
import com.lince.observer.desktop.javafx.generic.JavaFXLinceBaseController;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.math.service.DataHubService;
import com.lince.observer.transcoding.TranscodingProvider;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private ListView<String> logArea;

    @FXML
    private ListView<String> videoPlaylistView;

    @FXML
    private Pane pane;


    @FXML
    private void initialize() {
        log.info("--            Init RootLayoutController               --");
        logArea.setItems(JavaFXLogHelper.getFxLog());
    }

    public void lazyInit() {
        try {
            getMainLinceApp().getPrimaryStage().setOnCloseRequest(confirmCloseEventHandler);
            dataHubService = getMainLinceApp().getDataHubService();
            videoPlaylistView.setItems(dataHubService.getUserPlayList());
            videoPlaylistView.setCellFactory(lv -> {
                ListCell<String> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        try {
                            super.updateItem(item, empty);
                            setText(StringUtils.defaultString(item));
                        } catch (Exception e) {
                            log.error("updateItem in videoPlayList", e);
                            setText(StringUtils.EMPTY);
                        }
                    }
                };
                ContextMenu contextMenu = new ContextMenu();
                MenuItem editItem = new MenuItem();
                editItem.textProperty().bind(Bindings.format("Eliminar de la lista"));
                editItem.setOnAction(event -> {
                    String item = cell.getItem();
                    dataHubService.removeVideoItemByIdentifier(item);
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
        mainLinceApp.getAnalysisService().getAllObservations().clear();
        mainLinceApp.getCategoryService().clearSelectedObservationTool();
        Registro.loadNewInstance();//legacy
        LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
        fileHelper.setLinceProjectPath(null, mainLinceApp);
        JavaFXLogHelper.addLogInfo("Nuevo proyecto");
    }

    /**
     * Functional Code for opening a lince project
     *
     * @param f Consumer that accepts a File object
     */
    private void doOpenLincePlusProject(Consumer<File> f) {
        try {
            LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
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
                    LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
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
                    LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
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
                    LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
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
        LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
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
        LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
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
    private void handleSelectYoutubeVideo() {
        TextInputDialog dialog = new TextInputDialog("https://www.youtube.com");
        dialog.setTitle("Introduce youtube link");
        dialog.setHeaderText("Visit the video you want and paste the link directly here, please");
        dialog.setContentText("Youtube link");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (StringUtils.containsIgnoreCase(name, ServerAppParams.YOUTUBE_URL)) {
                dataHubService.addYoutubeVideoItem(name);
            } else {
                JavaFXLogHelper.showMessage(AlertType.ERROR, "Enlace invalido", "Tu enlace no parece ser de youtube");
            }

        });
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
        List<File> fileList = LinceDesktopFileHelper.openMultipleFileDialog(mainLinceApp, supportedTypes);
        boolean isFirstVideo = true;
        if (CollectionUtils.isNotEmpty(fileList)) {
            StringBuilder urls = new StringBuilder();
            boolean conversionDone = false;
            for (File file : fileList) {
                urls.append(String.format("-%s\n", file.getPath()));
                Predicate<String> p = (String videoType) ->
                        JavaFXLogHelper.showMessage(AlertType.CONFIRMATION
                                , "Video conversion"
                                , String.format("You have selected a video file in %s format. We are going to convert it to MP4. Please wait until it finishes, it will take some time :)"
                                        , videoType)).get() == ButtonType.OK;
                try {
                    File videoFile = transcodingProvider.reviewVideoFile(file, p);
                    if (videoFile != file) {
                        conversionDone = true;
                    }
                    dataHubService.addVideoItem(videoFile, transcodingProvider.getFPSFromVideo(isFirstVideo ? file : null));
                    isFirstVideo = false;
                    JavaFXLogHelper.showMessage(AlertType.INFORMATION, "Videos añadido", urls.toString());
                } catch (RuntimeException e) {
                    JavaFXLogHelper.addLogInfo("Conversion de video cancelada");
                }
            }

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
        dataHubService.getYoutubeVideoPlayList().clear();
        dataHubService.updateUserPlayList();
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
        JavaFXLoader.exit(getMainLinceApp());
    }


    @FXML
    private void handleOpenBrowser() {
        String url = mainLinceApp.getServerURL();
        JavaFXLogHelper.addLogInfo(i18n("open_browser", url));
        ServerValuesHelper.openLANLinceBrowser(url, false);
    }


    private String i18n(String key, String... args) {
        return getMainLinceApp().getMessage(key, (Object[]) args);
    }

    /**
     * Ensures data compatibility for export operations by migrating current data to legacy format.
     * This method initializes legacy instances and performs the migration process.
     *
     * @return Optional containing the selected research UUID if successful, empty Optional otherwise
     */
    private Optional<UUID> ensureExportCompatibility() {
        UUID researchId = getResearchSelection();
        return getMainLinceApp().getLegacyConverterService().ensureExportCompatibility(researchId);
    }

    /**
     * Ensures data compatibility for import operations by migrating legacy data to current format.
     * This method initializes legacy instances and performs the migration process.
     */
    private void ensureImportCompatibility() {
        getMainLinceApp().getLegacyConverterService().ensureImportCompatibility();
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
            }
            List<UserProfile> allUserProfiles = researchers.stream()
                    .flatMap(profile -> profile.getUserProfiles().stream())
                    .toList();

            if (allUserProfiles.size() <= 1) {
                return allUserProfiles.isEmpty() ? null : allUserProfiles.get(0).getRegisterCode();
            }
            Map<UUID, String> uuidToNameMap = allUserProfiles.stream()
                    .collect(Collectors.toMap(
                            UserProfile::getRegisterCode,
                            profile -> StringUtils.defaultIfEmpty(profile.getUserName(), "Sin definir")
                    ));

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(i18n("select_observer"));
            alert.setHeaderText(i18n("select_observer.header"));
            alert.setContentText(i18n("select_observer.content"));

            Map<ButtonType, UUID> buttonToUuidMap = uuidToNameMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> new ButtonType(entry.getValue()),
                            Map.Entry::getKey
                    ));

            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            buttonToUuidMap.put(cancelButton, null);

            alert.getButtonTypes().setAll(buttonToUuidMap.keySet());

            return alert.showAndWait()
                    .map(buttonToUuidMap::get)
                    .orElseGet(() -> allUserProfiles.get(0).getRegisterCode());
        } catch (Exception e) {
            JavaFXLogHelper.addLogError("Error in getResearchSelection", e);
            return null;
        }
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
     * Handles the export process for JavaFX components
     *
     * @param component JavaFX component to display
     * @param key       i18n key
     * @param label     i18n addon
     */
    private void doExportFX(javafx.scene.Node component, String key, String label) {
        String i18n = getMainLinceApp().getMessage(key, label);
        try {
            Registro.loadNewInstance();
//            ensureCompatibility(true); this makes the panel shown 2 times
            Stage stage = new Stage();
            stage.setTitle(i18n);
            Pane root = new Pane();
            root.getChildren().add(component);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(mainLinceApp.getPrimaryStage());
            stage.showAndWait();

            JavaFXLogHelper.addLogInfo(i18n);
        } catch (Exception e) {
            JavaFXLogHelper.addLogError(i18n, e);
        }
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
            if (isExport) {
                ensureExportCompatibility();
            } else {
                ensureImportCompatibility();
            }
            if (cmd != null) {
                cmd.execute();
            } else {
                openEmbedded(panel, i18n);
            }
            if (!isExport) {
                //solo es el caso de Hoisan y de ficheros de carga
                ensureImportCompatibility();
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
        ensureImportCompatibility();//para importar tenemos que migrar el instrumento!
        doImport(new LoadRegistro(), null, "panel_import_custom", i18n("import.lince-1.register"));
    }

    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleImportToolLince1() {
        doImport(new LoadInstrumentoObservacional(), null, "panel_import_custom", i18n("import.lince-1.observationTool"));
    }


    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleExportRegisterLince1() {
        ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(new SaveRegistroAs(), null, "panel_export_custom", i18n("import.lince-1.register"));
    }

    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleExportToolLince1() {
        ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(new SaveInstrumentoObservacionalAs(), null, "panel_export_custom", i18n("import.lince-1.observationTool"));
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
        ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        doExport(new AbrirExportarHoisan(), null, "panel_export_custom", "Hoisan");
    }

    /***
     * Valid export to theme 5.
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportTheme5() {
        Optional<UUID> projectId = ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        projectId.ifPresent(id -> doExportFX(new Theme5ExportComponent(projectId.get()), "panel_export_custom", "Theme 5"));
    }

    /***
     * Valid export to theme 6.
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportTheme6() {
        Optional<UUID> projectId = ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        projectId.ifPresent(id -> doExportFX(new Theme6ExportComponent(projectId.get()), "panel_export_custom", "Theme 6"));
    }

    /***
     * Valid export to SAS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSAS() {
        Optional<UUID> projectId = ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        projectId.ifPresent(id ->
                doExportFX(new SasExportComponent(projectId.get()), "panel_export_custom", "SAS")
        );
    }

    /***
     * Valid export to excel with csv file
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportExcel() {
        doExportFX(new CustomCsvExportComponent(getResearchSelection()), "panel_export_custom", "Excel");
    }

    private class CustomCsvExportComponent extends CsvExportComponent {

        protected CustomCsvExportComponent(UUID observerId) {
            super(observerId);
        }

        @Override
        protected List<Object> getSelectionItems() {
            setLinceProject(getCurrentProjectDetails());
            return super.getSelectionItems();
        }
    }


    public ILinceProject getCurrentProjectDetails() {
        ILinceProject linceProject = new LinceFileProjectWrapper();
        linceProject.setProfiles(dataHubService.getUserData());
        linceProject.setRegister(dataHubService.getDataRegister());
        linceProject.setObservationTool(dataHubService.getCriteria());
        linceProject.setVideoPlayList(dataHubService.getVideoPlayList());
        linceProject.setYoutubeVideoPlayList(dataHubService.getYoutubeVideoPlayList());
        return linceProject;
    }

    /***
     * Valid export to SDIS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQEstado() {
        Optional<UUID> projectId = ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        projectId.ifPresent(id ->
                doExportFX(new RegistroSdisGseqEstadoExport(projectId.get()), "panel_export_custom", "SDIS Seq por estados")
        );
    }

    /***
     * Valid export to SDIS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQEvent() {
        Optional<UUID> projectId = ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        projectId.ifPresent(id ->
                doExportFX(new RegistroSdisGseqEventExport(projectId.get()), "panel_export_custom", "SDIS Seq por eventos")
        );
    }

    /***
     * Valid export to SDIS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQTimeEvent() {
        Optional<UUID> projectId = ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        projectId.ifPresent(id ->
                doExportFX(new RegistroSdisGseqTimedEventExport(projectId.get()), "panel_export_custom", "SDIS Seq por eventos y tiempo")
        );
    }

    /***
     * Valid export to SDIS
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQInterval() {
        Optional<UUID> projectId = ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        projectId.ifPresent(id ->
                doExportFX(new RegistroSdisGseqIntervalExport(projectId.get()), "panel_export_custom", "SDIS Seq por intérvalos")
        );
    }

    /***
     * Valid export to SDIS.
     * Opens a legacy dialog with all previous Lince behaviour
     */
    @FXML
    private void handleExportSDISGSEQMultiEvent() {
        Optional<UUID> projectId = ensureExportCompatibility();//el new panel ya hace copia de contenido y necesita los datos legacy
        doExportFX(new RegistroSdisGseqMultieventExport(projectId.get()), "panel_export_custom", "SDIS Seq por eventos múltiples");
    }

    private final EventHandler<WindowEvent> confirmCloseEventHandler = event -> {
        try {
            Stage primaryStage = getMainLinceApp().getPrimaryStage();
            Alert closeConfirmation = new Alert(
                    AlertType.CONFIRMATION,
                    i18n("save.beforeClose")
            );
            Button exitButton = (Button) closeConfirmation.getDialogPane().lookupButton(
                    ButtonType.OK
            );
            exitButton.setText(i18n("yes"));
            Button noButton = (Button) closeConfirmation.getDialogPane().lookupButton(
                    ButtonType.CANCEL
            );
            noButton.setText(i18n("no"));
            closeConfirmation.setHeaderText(i18n("exit"));
            closeConfirmation.initModality(Modality.APPLICATION_MODAL);
            closeConfirmation.initOwner(primaryStage);

            closeConfirmation.setX(primaryStage.getX() + (primaryStage.getWidth() / 2 - 150));
            closeConfirmation.setY(primaryStage.getY() + (primaryStage.getHeight() / 2 - 100));

            closeConfirmation.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    handleSaveAs();
                }
                JavaFXLoader.exit(getMainLinceApp());
            });
        } catch (Exception e) {
            log.error("Error during close operation", e);
            JavaFXLoader.exit(getMainLinceApp()); // Ensure application shuts down even if there's an error
        }
    };
}
