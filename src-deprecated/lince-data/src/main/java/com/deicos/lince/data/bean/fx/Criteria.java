/*
 *  Lince - Automatizacion de datos observacionales
 *  Copyright (C) 2010  Brais Gabin Moreira
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.deicos.lince.data.bean.fx;

import javafx.beans.property.*;

import java.util.List;

/***
 *
 */
public class Criteria {


    private final StringProperty name;
    private final StringProperty description;
    private final SimpleBooleanProperty isPersistant;
    //private final ListProperty<Category> categories;

    public Criteria() {
        this(null, null, false, null);
    }

    public Criteria(String name, String description, boolean isPersistant, List<Category> categories) {
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.isPersistant = new SimpleBooleanProperty(isPersistant);
      /*  ObservableList<Category> aux = new FilteredList<Category>() ;
        this.categories = new SimpleListProperty<Category>(categories);*/
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public boolean getIsPersistant() {
        return isPersistant.get();
    }

    public SimpleBooleanProperty isPersistantProperty() {
        return isPersistant;
    }

    public void setIsPersistant(boolean isPersistant) {
        this.isPersistant.set(isPersistant);
    }
/*
    public ObservableList<Category> getCategories() {
        return categories.get();
    }

    public ListProperty<Category> categoriesProperty() {
        return categories;
    }

    public void setCategories(ObservableList<Category> categories) {
        this.categories.set(categories);
    }*/
}
