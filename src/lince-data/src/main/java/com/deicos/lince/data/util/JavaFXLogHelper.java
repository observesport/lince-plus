package com.deicos.lince.data.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * com.deicos.lince.app.helper
 * Class JavaFXLogHelper
 * @author berto (alberto.soto@gmail.com). 19/04/2018
 */
public class JavaFXLogHelper {
    /**
     * logger mapper
     */
    private static ObservableList<String> fxLog = FXCollections.observableArrayList();

    public static ObservableList<String> getFxLog() {
        return fxLog;
    }

    public static void showMessageDialog(String header, String content) {
        JavaFXLogHelper.showMessageDialog(Alert.AlertType.INFORMATION, header, content);
    }

    public static void showMessageDialog(Alert.AlertType type, String header, String content) {
        TextArea area = new TextArea(content);
        area.setWrapText(true);
        area.setEditable(false);

        Alert alert = new Alert(type);
        alert.setTitle(header);
        alert.setHeaderText(header);
        alert.getDialogPane().setContent(area);
        alert.setResizable(true);
        alert.showAndWait();
    }

    /**
     * Añade un mensaje info a la cola observable de usuario
     *
     * @param msg
     */
    public static void addLogInfo(String msg) {
        addLogError(msg, null);
    }

    /**
     * Añade un mensaje de error a la cola observable de usuario
     *
     * @param msg
     * @param e
     */
    public static void addLogError(String msg, Exception e) {
        if (e == null) {
            LoggerFactory.getLogger(JavaFXLogHelper.class).info(msg);
            fxLog.add("INFO: " + msg);
        } else {
            LoggerFactory.getLogger(JavaFXLogHelper.class).error(msg, e);
            fxLog.addAll("ERROR: " + msg + " - " + e.toString());
        }
    }

    public static Optional<ButtonType> showMessage(Alert.AlertType alertType, String header, String txt) {
        return JavaFXLogHelper.showMessage(alertType, header, txt, null);
    }
    public static Optional<ButtonType> showMessage(Alert.AlertType alertType, String header, String txt, Stage stage) {
        try{
            Alert alert = new Alert(alertType);
            String title;
            switch (alertType) {
                case NONE:
                    title = "";
                    break;
                case INFORMATION:
                    title = "Información";
                    break;
                case WARNING:
                    title = "Cuidado!";
                    break;
                case CONFIRMATION:
                    title = "Operación finalizada correctamente";
                    break;
                case ERROR:
                    title = "Error";
                    break;
                default:
                    title = "Mensaje de sistema";
                    break;
            }
            if (stage != null) {
                alert.initOwner(stage);
            }
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(txt);
            return alert.showAndWait();
        }catch (Exception e){
            LoggerFactory.getLogger(JavaFXLogHelper.class).error("No javafx environment found");
        }

        return null;
    }

}
