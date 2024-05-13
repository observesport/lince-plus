package com.deicos.lince.app.javafx.view;

import com.deicos.lince.app.LinceApp;
import com.deicos.lince.app.javafx.generic.JavaFXLinceBaseController;
import com.deicos.lince.data.bean.example.Person;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeTableView;

/**
 * lince-scientific-base
 * com.deicos.lince.app.javafx.view
 * @author berto (alberto.soto@gmail.com)in 28/02/2017.
 * Description:
 */
public class DataCategoriesController extends JavaFXLinceBaseController {

    //LinceDataHelper dataHelper = LinceDataHelper.getInstance();

    @FXML
    private TreeTableView<Person> personTable;
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
    private void initialize() {
        // Initialize the person table with the two columns.
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        // Clear person details.
        //showPersonDetails(null);
        // Listen for selection changes and show the person details when changed.
        //personTable.getSelectionModel().selectedItemProperty().addListener(
//                (observable, oldValue, newValue) -> showPersonDetails(newValue));
    }
    private void showPersonDetails(Person person) {
        if (person != null) {
            // Fill the labels with info from the person object.
            firstNameLabel.setText(person.getFirstName());
            lastNameLabel.setText(person.getLastName());
            streetLabel.setText(person.getStreet());

        } else {
            // Person is null, remove all the text.
            firstNameLabel.setText("");
            lastNameLabel.setText("");
            streetLabel.setText("");

        }
    }
    public void setMainLinceApp(LinceApp mainLinceApp) {
        this.mainLinceApp = mainLinceApp;
        // Add observable list data to the table
        //personTable.setItems(mainApp.getAllResearchInfo());
    }
}
