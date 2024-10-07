package com.lince.observer.data.bean.wrapper;

import com.lince.observer.data.bean.categories.Category;

import java.io.Serializable;
import java.util.List;

/**
 * lince-scientific-base
 * com.lince.observer.data.bean.wrapper
 * Created by Alberto Soto Fernandez in 15/03/2017.
 * Description:
 */
public class SceneWrapper implements Serializable {
    protected Integer id;
    protected Double moment;
    protected String name;
    protected String description;
    protected List<Category> categories;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getMoment() {
        return moment;
    }
    public void setMoment(Double moment) {
        this.moment = moment;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
