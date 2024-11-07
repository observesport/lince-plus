package com.lince.observer.data.javafx;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;
/**
 * Created by Alberto Soto. 7/11/24
 */
public class SelectionPanelComponent extends VBox {

    private final ListView<Object> availableItems;
    private final ListView<Object> selectedItems;
    private final ObservableList<Object> availableList;
    private final ObservableList<Object> selectedList;

    public SelectionPanelComponent(Object[] objects, String name1, String name2) {
        availableList = FXCollections.observableArrayList(objects);
        selectedList = FXCollections.observableArrayList();

        availableItems = new ListView<>(availableList);
        selectedItems = new ListView<>(selectedList);

        availableItems.setCellFactory(param -> new DraggableListCell());
        selectedItems.setCellFactory(param -> new DraggableListCell());

        VBox buttonsBox = getvBox();

        HBox listsBox = new HBox(10, availableItems, buttonsBox, selectedItems);
        HBox.setHgrow(availableItems, Priority.ALWAYS);
        HBox.setHgrow(selectedItems, Priority.ALWAYS);

        setSpacing(10);
        setPadding(new Insets(10));
        getChildren().addAll(listsBox);
    }

    private VBox getvBox() {
        Button addButton = new Button(">");
        Button removeButton = new Button("<");
        Button addAllButton = new Button(">>");
        Button removeAllButton = new Button("<<");

        addButton.setOnAction(e -> moveItems(availableItems, selectedItems));
        removeButton.setOnAction(e -> moveItems(selectedItems, availableItems));
        addAllButton.setOnAction(e -> moveAllItems(availableList, selectedList));
        removeAllButton.setOnAction(e -> moveAllItems(selectedList, availableList));

        VBox buttonsBox = new VBox(10, addButton, removeButton, addAllButton, removeAllButton);
        buttonsBox.setPadding(new Insets(5));
        return buttonsBox;
    }

    private void moveItems(ListView<Object> from, ListView<Object> to) {
        ObservableList<Object> selectedItems = from.getSelectionModel().getSelectedItems();
        to.getItems().addAll(selectedItems);
        from.getItems().removeAll(selectedItems);
    }

    private void moveAllItems(ObservableList<Object> from, ObservableList<Object> to) {
        to.addAll(from);
        from.clear();
    }

    public List<Object> getSelectedElements() {
        return selectedList;
    }

    public List<Object> getNonSelectedElements() {
        return availableList;
    }

    private class DraggableListCell extends ListCell<Object> {
        public DraggableListCell() {
            setOnDragDetected(event -> {
                if (getItem() == null) return;
                startDragAndDrop().setContent(null);
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragDropped(event -> {
                if (getItem() == null) {
                    return;
                }
                ObservableList<Object> items = getListView().getItems();
                int draggedIdx = items.indexOf(event.getDragboard().getString());
                int thisIdx = items.indexOf(getItem());
                items.set(draggedIdx, getItem());
                items.set(thisIdx, event.getDragboard().getString());
                event.setDropCompleted(true);
                event.consume();
            });
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item.toString());
        }
    }
}
