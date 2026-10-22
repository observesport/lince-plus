package com.lince.observer.desktop.javafx.view;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.bean.transfer.ExportRequest;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.transfer.ImportResult;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.javafx.*;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.service.ImportService;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.exporters.HoisanExporter;
import com.lince.observer.data.transfer.exporters.Lince1RegisterExporter;
import com.lince.observer.data.transfer.exporters.Lince1ToolExporter;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.desktop.ServerAppParams;
import com.lince.observer.desktop.helper.ServerValuesHelper;
import com.lince.observer.desktop.javafx.JavaFXLoader;
import com.lince.observer.desktop.javafx.components.JavaFXBrowser;
import com.lince.observer.desktop.javafx.generic.JavaFXLinceBaseController;
import com.lince.observer.desktop.spring.service.CurrentProjectProvider;
import com.lince.observer.desktop.spring.service.ExternalLinkService;
import com.lince.observer.legacy.export.GseqExporterFactory.GseqFormatType;
import com.lince.observer.math.service.DataHubService;
import com.lince.observer.transcoding.TranscodingProvider;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
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

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.geometry.Insets;

/**
 * The controller for the root layout. The root layout provides the basic
 * application layout containing a menu bar and space where other JavaFX
 * elements can be placed.
 *
 * @author Alberto Soto Fernandez
 */
@Component
public class RootLayoutController extends JavaFXLinceBaseController {

    /* LO-349 import type ids, see GET /api/import/types */
    private static final String IMPORT_TYPE_LINCE_PLUS = "lince-plus";
    private static final String IMPORT_TYPE_LINCE1_REGISTER = "lince1-register";
    private static final String IMPORT_TYPE_LINCE1_TOOL = "lince1-tool";
    private static final String IMPORT_TYPE_HOISAN = "hoisan";
    private static final FileChooser.ExtensionFilter XML_FILTER = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
    private static final FileChooser.ExtensionFilter RLINCE_FILTER = new FileChooser.ExtensionFilter("Lince 1 (*.rlince)", "*.rlince");
    private static final FileChooser.ExtensionFilter ILINCE_FILTER = new FileChooser.ExtensionFilter("Lince 1 (*.ilince)", "*.ilince");
    private static final FileChooser.ExtensionFilter MDB_FILTER = new FileChooser.ExtensionFilter("HOISAN (*.mdb)", "*.mdb");

    @Autowired
    protected DataHubService dataHubService;

    @Autowired
    protected ExternalLinkService externalLinkService;

    @Autowired
    protected ImportService importService;

    @Autowired
    protected ExportService exportService;

    @Autowired
    protected CurrentProjectProvider currentProjectProvider;

    protected TranscodingProvider transcodingProvider = null;

    @FXML
    private ListView<String> logArea;

    @FXML
    private ListView<String> videoPlaylistView;

    @FXML
    private MenuItem ngrokMenuItem;

    private BooleanProperty ngrokRunning = new SimpleBooleanProperty(false);

    @FXML
    private Pane pane;


    @FXML
    private void initialize() {
        log.info("--            Init RootLayoutController               --");
        logArea.setItems(JavaFXLogHelper.getFxLog());
        ngrokRunning.addListener((obs, wasRunning, isRunning) -> {
            if (isRunning) {
                ngrokMenuItem.setText(i18n("desktop.option.browser.ngrok.close"));
            } else {
                ngrokMenuItem.setText(i18n("desktop.option.browser.ngrok.open"));
            }
        });
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
            JavaFXBrowser browser = new JavaFXBrowser(getMainLinceApp().getServerURL() + "/desktop/index.html");
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
        currentProjectProvider.resetLegacyRegister();//legacy, under the LegacyBridge lock
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
        doOpenFile(XML_FILTER, f);
    }

    /**
     * Opens a FileChooser filtered by one extension and runs the consumer with the selected file
     *
     * @param filter extension filter
     * @param f      Consumer that accepts a File object
     */
    private void doOpenFile(FileChooser.ExtensionFilter filter, Consumer<File> f) {
        try {
            LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
            FileChooser fileChooser = fileHelper.getFileChooser();

            // Set extension filter
            fileChooser.getExtensionFilters().add(filter);
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
     * Imports the selected file through {@link ImportService} (LO-349), the same path {@code POST /api/import} uses
     *
     * @param title       i18n title for log lines and error dialogs
     * @param filter      extension filter
     * @param type        import type id
     * @param scope       import scope
     * @param doneMessage log line on success
     */
    private void doImportFile(String title, FileChooser.ExtensionFilter filter, String type, ImportScope scope,
                              String doneMessage) {
        doOpenFile(filter, file -> {
            try (InputStream in = Files.newInputStream(file.toPath())) {
                ImportResult result = importService.importFile(file.getName(), in, type, scope, Map.of());
                JavaFXLogHelper.addLogInfo(doneMessage + " (" + file.getName() + "): "
                        + result.criteriaCount() + " criterios, " + result.registerCount() + " observadores, "
                        + result.observationCount() + " observaciones");
                result.warnings().forEach(warning -> JavaFXLogHelper.addLogInfo(title + ": " + warning));
            } catch (TransferException e) {
                log.warn("{}: {} {}", title, e.getReason(), e.getMessage());
                JavaFXLogHelper.showMessage(AlertType.ERROR, title, e.getMessage());
            } catch (Exception e) {
                JavaFXLogHelper.addLogError(title, e);
            }
        });
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
        doImportFile(getMainLinceApp().getMessage("panel_import_custom", "Lince PLUS"), XML_FILTER,
                IMPORT_TYPE_LINCE_PLUS, ImportScope.FULL,
                "Se ha cargado el instrumento y la observación del proyecto seleccionado");
    }


    /**
     * Opens a FileChooser to let the user select a project file
     * It only adds all observations into current project
     */
    @FXML
    private void handleImportLincePlusObserver() {
        doImportFile(getMainLinceApp().getMessage("panel_import_custom", "Lince PLUS"), XML_FILTER,
                IMPORT_TYPE_LINCE_PLUS, ImportScope.OBSERVATIONS,
                "Se han importado los datos de observación del proyecto seleccionado");
    }

    /**
     * Saves the file to the person file that is currently open. If there is no
     * open file, the "save as" dialog is shown.
     */
    @FXML
    private void handleSave() {
        LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
        File file = fileHelper.getLinceProjectFilePath();
        if (file != null && !file.isDirectory()) {
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


    /**
     * Opens the new frontend in the browser.
     * This method opens /index.html which contains the new React app.
     */
    @FXML
    private void handleOpenBrowser() {
        String url = mainLinceApp.getServerURL();// + "/index.html";
        JavaFXLogHelper.addLogInfo(i18n("open_browser", url));
        ServerValuesHelper.openLANLinceBrowser(url, false);
    }

    @FXML
    private void handleNgrokToggle() {
        if (ngrokRunning.get()) {
            externalLinkService.disconnect();
            ngrokRunning.set(false);
            getMainLinceApp().regenerateQRCode();
            JavaFXLogHelper.showMessage(AlertType.INFORMATION, "Ngrok Stopped", "Ngrok service has been stopped.");
        } else {
            handleNgrokStart();
        }
    }

    private void handleNgrokStart() {
        if (externalLinkService == null) {
            try {
                externalLinkService = getMainLinceApp().getExternalLinkService();
            } catch (Exception e) {
                JavaFXLogHelper.showMessage(AlertType.ERROR, "Ngrok Service Error",
                        "Failed to initialize Ngrok service. Please check your configuration.");
                JavaFXLogHelper.addLogError("Failed to initialize Ngrok service", e);
                return;
            }
        }

        if (externalLinkService == null) {
            JavaFXLogHelper.showMessage(AlertType.ERROR, "Ngrok Service Error",
                    "Ngrok service is not available. Please check your configuration.");
            return;
        }

        Map<String, String> configParams = externalLinkService.getConfigurationParameters();
        Map<String, String> userInputs = new HashMap<>();

        boolean allInputsProvided = showConfigurationDialog(configParams, userInputs);
        JavaFXLogHelper.addLogInfo("User inputs: " + userInputs);
        if (allInputsProvided) {
            externalLinkService.configureService(userInputs);
            String externalLink = externalLinkService.generateLink(getMainLinceApp().getCurrentPort());
            if (!StringUtils.isEmpty(externalLink)) {
                JavaFXLogHelper.addLogInfo("Ngrok located at " + externalLink);
                getMainLinceApp().regenerateQRCode();
                showNgrokSuccessDialog(externalLink);
            } else {
                String errorMsg = externalLinkService.getLastError();
                if (StringUtils.isEmpty(errorMsg)) {
                    errorMsg = "Ngrok failed to generate an external link. Please check the log for details.";
                }
                JavaFXLogHelper.addLogError("Ngrok connection failed: " + errorMsg, null);
                JavaFXLogHelper.showMessage(AlertType.ERROR, "Ngrok Connection Failed", errorMsg);
            }
            ngrokRunning.set(externalLinkService.isConnected());
        } else {
            JavaFXLogHelper.showMessage(AlertType.WARNING, "Ngrok Configuration", "Ngrok configuration was cancelled or incomplete.");
        }
    }

    private String i18n(String key, String... args) {
        return getMainLinceApp().getMessage(key, (Object[]) args);
    }

    /**
     * Muestra un panel para seleccionar el observador si existe más de uno en el proyecto.
     * En caso contrario devuelve null
     *
     * @return UUID de observador seleccionado o nulo por defecto
     */
    private UUID getResearchSelection() {
        try {
            List<UserProfile> allUserProfiles = currentProjectProvider.userProfiles();
            if (allUserProfiles.size() <= 1) {
                return currentProjectProvider.currentResearchId();
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
     * Shows an export panel ({@link GenericExportComponent}) as a modal window
     *
     * @param component export panel
     * @param key       i18n key
     * @param label     i18n addon
     */
    private void doExportFX(javafx.scene.Node component, String key, String label) {
        String i18n = getMainLinceApp().getMessage(key, label);
        try {
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
     * Exports a whole-project type (no criteria panel) through {@link ExportService} (LO-349), the same path
     * {@code POST /api/export} uses, and writes it to the file chosen in a save {@link FileChooser}. As in
     * {@code GenericExportComponent.saveFiles}: the dialog comes first (cancelling exports nothing), and any
     * existing target other than the chosen file ({@link ExportTargets#toConfirm}) is confirmed before writing.
     *
     * @param type        export type id
     * @param researchId  observer to export, null for the default one
     * @param title       i18n title for log lines and dialogs
     * @param filter      extension filter
     */
    private void doExportFile(String type, UUID researchId, String title, FileChooser.ExtensionFilter filter) {
        FileChooser fileChooser = new LinceDesktopFileHelper().getFileChooser();
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showSaveDialog(mainLinceApp.getPrimaryStage());
        if (file == null) {
            return;
        }
        List<ExportedFile> files;
        try {
            files = exportService.export(new ExportRequest(type, researchId, List.of(), Map.of()));
        } catch (TransferException e) {
            log.warn("{}: {} {}", title, e.getReason(), e.getMessage());
            JavaFXLogHelper.showMessage(AlertType.ERROR, title, e.getMessage());
            return;
        } catch (Exception e) {
            JavaFXLogHelper.addLogError(title, e);
            return;
        }
        try {
            List<ExportTargets.Target> targets = ExportTargets.resolve(file, files);
            if (!confirmOverwrite(title, ExportTargets.toConfirm(file, targets))) {
                return;
            }
            ExportTargets.write(targets);
            JavaFXLogHelper.addLogInfo(title + ": " + targets.get(0).file().getAbsolutePath());
            JavaFXLogHelper.showMessage(AlertType.INFORMATION, title, i18n("FILE_SAVED"));
        } catch (Exception e) {
            JavaFXLogHelper.addLogError(title, e);
        }
    }

    /**
     * Asks before overwriting existing export targets other than the file the save dialog already confirmed.
     *
     * @return true when there is nothing to confirm or the user accepts
     */
    private boolean confirmOverwrite(String title, List<File> existing) {
        if (existing.isEmpty()) {
            return true;
        }
        Alert confirm = new Alert(AlertType.CONFIRMATION, i18n("OVERWRITE_FILE_PROMPT") + "\n"
                + existing.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")),
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle(title);
        confirm.setHeaderText(null);
        return confirm.showAndWait().filter(ButtonType.YES::equals).isPresent();
    }

    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleImportRegisterLince1() {
        String title = getMainLinceApp().getMessage("panel_import_custom", i18n("import.lince-1.register"));
        doImportFile(title, RLINCE_FILTER, IMPORT_TYPE_LINCE1_REGISTER, ImportScope.FULL, title);
    }

    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleImportToolLince1() {
        String title = getMainLinceApp().getMessage("panel_import_custom", i18n("import.lince-1.observationTool"));
        doImportFile(title, ILINCE_FILTER, IMPORT_TYPE_LINCE1_TOOL, ImportScope.FULL, title);
    }


    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleExportRegisterLince1() {
        doExportFile(Lince1RegisterExporter.TYPE_ID, getResearchSelection(),
                getMainLinceApp().getMessage("panel_export_custom", i18n("import.lince-1.register")),
                RLINCE_FILTER);
    }

    /**
     * Legacy version compatibility
     */
    @FXML
    private void handleExportToolLince1() {
        doExportFile(Lince1ToolExporter.TYPE_ID, null,
                getMainLinceApp().getMessage("panel_export_custom", i18n("import.lince-1.observationTool")),
                ILINCE_FILTER);
    }

    /**
     * HOISAN compatibility
     */
    @FXML
    private void handleImportHoisan() {
        String title = getMainLinceApp().getMessage("panel_import_custom", "Hoisan");
        doImportFile(title, MDB_FILTER, IMPORT_TYPE_HOISAN, ImportScope.FULL, title);
    }

    /**
     * HOISAN compatibility
     */
    @FXML
    private void handleExportHoisan() {
        doExportFile(HoisanExporter.TYPE_ID, getResearchSelection(),
                getMainLinceApp().getMessage("panel_export_custom", "Hoisan"), MDB_FILTER);
    }

    /***
     * Theme 5 export (.csv register + .vvt instrument) through {@link ExportService}
     */
    @FXML
    private void handleExportTheme5() {
        doExportFX(new Theme5ExportComponent(getCurrentProjectDetails(), getResearchSelection(), exportService),
                "panel_export_custom", "Theme 5");
    }

    /***
     * Theme 6 export (.txt register + vvt.vvt instrument) through {@link ExportService}
     */
    @FXML
    private void handleExportTheme6() {
        doExportFX(new Theme6ExportComponent(getCurrentProjectDetails(), getResearchSelection(), exportService),
                "panel_export_custom", "Theme 6");
    }

    /***
     * SAS export through {@link ExportService}
     */
    @FXML
    private void handleExportSAS() {
        doExportFX(new SasExportComponent(getCurrentProjectDetails(), getResearchSelection(), exportService),
                "panel_export_custom", "SAS");
    }

    /***
     * CSV (Excel compatible) export through {@link ExportService}
     */
    @FXML
    private void handleExportExcel() {
        doExportFX(new CsvExportComponent(getCurrentProjectDetails(), getResearchSelection(), exportService),
                "panel_export_custom", "Excel");
    }

    public ILinceProject getCurrentProjectDetails() {
        return currentProjectProvider.current();
    }

    /***
     * SDIS-GSEQ export in one format through {@link ExportService}
     *
     * @param format SDIS-GSEQ format
     * @param label  window title addon
     */
    private void doExportSdisGseq(GseqFormatType format, String label) {
        doExportFX(new SdisGseqExportComponent(getCurrentProjectDetails(), getResearchSelection(), exportService, format),
                "panel_export_custom", label);
    }

    @FXML
    private void handleExportSDISGSEQEstado() {
        doExportSdisGseq(GseqFormatType.STATE, "SDIS Seq por estados");
    }

    @FXML
    private void handleExportSDISGSEQEvent() {
        doExportSdisGseq(GseqFormatType.EVENT, "SDIS Seq por eventos");
    }

    @FXML
    private void handleExportSDISGSEQTimeEvent() {
        doExportSdisGseq(GseqFormatType.TIMED, "SDIS Seq por eventos y tiempo");
    }

    @FXML
    private void handleExportSDISGSEQInterval() {
        doExportSdisGseq(GseqFormatType.INTERVAL, "SDIS Seq por intérvalos");
    }

    @FXML
    private void handleExportSDISGSEQMultiEvent() {
        doExportSdisGseq(GseqFormatType.MULTIEVENT, "SDIS Seq por eventos múltiples");
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

    private void showNgrokSuccessDialog(String externalLink) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Ngrok Started");
        alert.setHeaderText("External link is now active!");
        alert.setContentText("Your application is now accessible via:\n" + externalLink);

        ButtonType copyButtonType = new ButtonType("Copy URL");
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(copyButtonType, okButtonType);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == copyButtonType) {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(externalLink);
                clipboard.setContent(content);
                JavaFXLogHelper.addLogInfo("Ngrok URL copied to clipboard: " + externalLink);
            }
        });
    }

    private boolean showConfigurationDialog(Map<String, String> configParams, Map<String, String> userInputs) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ngrok Configuration");
        dialog.setHeaderText("Please provide the required configuration parameters:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        String configMessage = externalLinkService.getConfigurationMessage();
        Label messageLabel = new Label(configMessage);
        messageLabel.setWrapText(true);

        Button openBrowserButton = new Button("Open Browser");
        openBrowserButton.setOnAction(e -> {
            Pattern pattern = Pattern.compile("https://[^\\s]+");
            Matcher matcher = pattern.matcher(configMessage);
            if (matcher.find()) {
                String externalUrl = matcher.group();
                ServerValuesHelper.openLANLinceBrowser(externalUrl, false);
            } else {
                JavaFXLogHelper.showMessage(AlertType.ERROR, "Error", "No external URL found in the configuration message.");
            }
        });
        grid.add(messageLabel, 0, 0);
        GridPane.setColumnSpan(messageLabel, GridPane.REMAINING);
        grid.addRow(1, openBrowserButton);
        Map<String, TextField> textFields = new HashMap<>();
        int row = 2;
        for (Map.Entry<String, String> entry : configParams.entrySet()) {
            String key = entry.getKey();
            String description = entry.getValue();

            Label label = new Label(description + ":");
            TextField textField = new TextField();
            textFields.put(key, textField);
            grid.addRow(row, label, textField);
            row++;
        }

        dialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okButtonType) {
            for (Map.Entry<String, TextField> entry : textFields.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().getText();
                userInputs.put(key, value);
            }
            return true;
        }
        return false;
    }
}
