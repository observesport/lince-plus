package com.lince.observer.data.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by Alberto Soto. 31/3/24
 *
 * Helper function for easy transition reminder from list to fxcollection helper
 */
public class FxCollectionHelper {

    public static <T> ObservableList<T> setObservableList(ObservableList<T> collection, List<T> item) {
        if (collection==null)
            return getObservableList(item);
        collection.clear();
        collection.setAll(item);
        return collection;
    }

    public static <T> ObservableList<T> getObservableList(List<T> item) {
        return FXCollections.observableArrayList(item);
    }

}
