package com.lince.observer.data.system.operations;

import com.lince.observer.data.base.ILinceApp;
import com.lince.observer.data.base.ILinceAppProvider;
import com.lince.observer.data.base.LinceFileHelperFx;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * lince-scientific-base
 * com.lince.observer.app
 *
 * @author berto (alberto.soto@gmail.com)in 24/06/2019.
 * Description:
 * <p>
 * Utils for opening a saving files in a stable environment
 * <p>
 * Tested on Win and MacOs
 */
public class LinceDesktopFileHelper extends LinceFileHelperFx {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * Get a file chooser depending on previous project openings
     *
     * @return user file chooser
     */
    public FileChooser getFileChooser() {
        FileChooser fileChooser = new FileChooser();
        try {
            File path = getLinceProjectFilePath();
            if (path != null && path.exists()) {
                path = path.getParentFile();
            }else{
                path = FileUtils.getUserDirectory();
            }
            fileChooser.setInitialDirectory(path);
        } catch (Exception e) {
            log.error("on file chooser", e);
        }
        return fileChooser;
    }

    /**
     * Abre un cuadro de dialogo para seleccionar fichero
     * Multiple file loading dialog
     *
     * @param myapp      link al  main app
     * @param parameters Tuplas de descripcion+filtro "XML files (*.xml)", "*.xml"
     * @return selected File to select if its null or not
     */
    public static List<File> openMultipleFileDialog(ILinceApp myapp, List<Pair<String, String>> parameters) {
        FileChooser chooser = getFileDialog(myapp, parameters);
        return chooser.showOpenMultipleDialog(myapp.getPrimaryStage());
    }

    /**
     * Load file dialog
     *
     * @param myapp      link al  main app
     * @param parameters Tuplas de descripcion+filtro "XML files (*.xml)", "*.xml"
     * @return selected File to select if its null or not
     */
    public static File openSingleFileDialog(ILinceApp myapp, List<Pair<String, String>> parameters) {
        FileChooser chooser = getFileDialog(myapp, parameters);
        return chooser.showOpenDialog(myapp.getPrimaryStage());
    }

    public static File openSingleFileDialogBasic(ILinceApp myapp, Pair<String, String> parameters) {
        List<Pair<String, String>> aux = new ArrayList<>();
        aux.add(parameters);
        return openSingleFileDialog(myapp, aux);
    }

    /**
     * Save to dialog
     *
     * @param myapp      link al  main app
     * @param parameters Tuplas de descripcion+filtro "XML files (*.xml)", "*.xml"
     * @return selected File to select if its null or not
     */
    public static File openSaveFileDialog(ILinceApp myapp, List<Pair<String, String>> parameters) {
        FileChooser chooser = getFileDialog(myapp, parameters);
        return chooser.showSaveDialog(myapp.getPrimaryStage());
    }

    public static File openSaveFileDialogBasic(ILinceApp myapp, Pair<String, String> parameters) {
        List<Pair<String, String>> aux = new ArrayList<>();
        aux.add(parameters);
        return openSaveFileDialog(myapp, aux);
    }

    /**
     * Basic save file dialog
     *
     * @param extension *.something
     * @return selected file
     */
    public static File openSaveFileDialog(String extension) {
        ILinceApp linceApp = ILinceAppProvider.getInstance();
        List<Pair<String, String>> types = new ArrayList<>();
        types.add(new MutablePair<>(extension, extension));
        return openSaveFileDialog(linceApp, types);
    }

    /**
     * Creates a working file chooser dialog with expected supported file types
     *
     * @param myapp      ilinceapp
     * @param parameters Pairs of description + format ("XML files (*.xml)", "*.xml")
     * @return fileChooser
     */
    private static FileChooser getFileDialog(ILinceApp myapp, List<Pair<String, String>> parameters) {
        LinceDesktopFileHelper fileHelper = new LinceDesktopFileHelper();
        FileChooser fileChooser = fileHelper.getFileChooser();
        // Set extension filter
        for (Pair<String, String> item : parameters) {
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(item.getKey(), item.getValue());
            fileChooser.getExtensionFilters().add(extFilter);
        }
        return fileChooser;
    }



}
