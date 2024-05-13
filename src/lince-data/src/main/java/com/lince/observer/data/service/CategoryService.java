package com.lince.observer.data.service;

import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import org.apache.commons.math3.util.Pair;

import java.util.List;

/**
 * Created by Alberto Soto. 30/3/24
 */
public interface CategoryService {
    List<CategoryData> getCollection();

    List<Criteria> getCriteria();

    List<CategoryData> getChildren(Integer id);

    Pair<Criteria, Category> findDataById(Integer id, String code, String name);

    CategoryData findCategoryById(Integer id);

    CategoryData findCategoryByCode(String code);

    CategoryData findCategoryByName(String name);

    void pushAll(List<Criteria> data);

    void checkToolIntegrity(List<Criteria> newData);

    void clearAll();
}
