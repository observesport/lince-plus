package com.lince.observer.data.bean.categories;

import com.lince.observer.data.LinceDataConstants;

public class Category extends CategoryData {

    public Category() {
        super();
    }

    @Override
    public boolean isCategory() {
        return true;
    }

    @Override
    public String getCategoryDataPrefix() {
        return LinceDataConstants.CATEGORY_PREFIX;
    }

    public Category(Integer id, String name, Integer parent) {
        super(id, name, 1);
        this.setParent(parent);
    }
}
