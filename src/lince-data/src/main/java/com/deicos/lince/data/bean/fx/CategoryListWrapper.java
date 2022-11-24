package com.deicos.lince.data.bean.fx;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * lince-scientific-base
 * com.deicos.lince.data
 * @author berto (alberto.soto@gmail.com)in 28/02/2017.
 * Description:
 */
@XmlRootElement(name = "categories")
public class CategoryListWrapper {
    private List<Category> categories;
    @XmlElement(name = "category")
    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
