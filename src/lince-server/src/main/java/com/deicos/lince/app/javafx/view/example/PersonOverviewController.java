package com.deicos.lince.app.javafx.view.example;

import com.deicos.lince.app.LinceApp;
import com.deicos.lince.app.javafx.JavaFXLoader;
import com.deicos.lince.app.javafx.generic.JavaFXLinceBaseController;
import com.deicos.lince.data.bean.example.Person;
import com.deicos.lince.data.bean.user.ResearchProfile;
import com.deicos.lince.data.util.DateUtil;
import com.deicos.lince.data.util.JavaFXLogHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class PersonOverviewController extends JavaFXLinceBaseController {

    @FXML
    private TableView<Person> personTable;
    @FXML
    private TableColumn<Person, String> firstNameColumn;
    @FXML
    private TableColumn<Person, String> lastNameColumn;

    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label streetLabel;
    @FXML
    private Label postalCodeLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label birthdayLabel;

    // Reference to the main application.
    private LinceApp mainLinceApp;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public PersonOverviewController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        // Clear person details.
        showPersonDetails(null);
        // Listen for selection changes and show the person details when changed.
        personTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPersonDetails(newValue));
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainLinceApp
     */
    public void setMainLinceApp(LinceApp mainLinceApp) {
        this.mainLinceApp = mainLinceApp;
        //TODO: 13/04/2018 Cambiar ventana profile a nuevo datahub
        List<ResearchProfile> users = mainLinceApp.getDataHubService().getUserData();
        // Add observable list data to the table
        ObservableList<Person> persons = FXCollections.observableArrayList();
        personTable.setItems(persons);
    }

    /**
     * Fills all text fields to show details about the person.
     * If the specified person is null, all text fields are cleared.
     *
     * @param person the person or null
     */
    private void showPersonDetails(Person person) {
        if (person != null) {
            // Fill the labels with info from the person object.
            firstNameLabel.setText(person.getFirstName());
            lastNameLabel.setText(person.getLastName());
            streetLabel.setText(person.getStreet());
            postalCodeLabel.setText(Integer.toString(person.getPostalCode()));
            cityLabel.setText(person.getCity());
            birthdayLabel.setText(DateUtil.format(person.getBirthday()));
        } else {
            // Person is null, remove all the text.
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            streetLabel.setText("");
            postalCodeLabel.setText("");
            cityLabel.setText("");
            birthdayLabel.setText("");
        }
    }

    /**
     * Called when the user clicks on the delete button.
     */
    @FXML
    private void handleDeletePerson() {
        int selectedIndex = personTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            personTable.getItems().remove(selectedIndex);
        } else {
            // Nothing selected.
            JavaFXLogHelper.showMessage(AlertType.WARNING
                    , "No Person Selected"
                    , "Please select a person in the table."
                    , mainLinceApp.getPrimaryStage());
        }
    }

    /**
     * Called when the user clicks the new button. Opens a dialog to edit
     * details for a new person.
     */
    @FXML
    private void handleNewPerson() {
        Person tempPerson = new Person();
        boolean okClicked = mainLinceApp.showPersonEditDialog(tempPerson);
        if (okClicked) {
            ResearchProfile profile = new ResearchProfile();
            mainLinceApp.getDataHubService().getUserData().add(profile);
        }
    }

    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected person.
     */
    @FXML
    private void handleEditPerson() {
        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            boolean okClicked = mainLinceApp.showPersonEditDialog(selectedPerson);
            if (okClicked) {
                showPersonDetails(selectedPerson);
            }

        } else {
            // Nothing selected.

            JavaFXLogHelper.showMessage(AlertType.WARNING
                    , getMainLinceApp().getMessage("no_person")
                    , "Please select a person in the table."
                    , mainLinceApp.getPrimaryStage());
        }
    }
}