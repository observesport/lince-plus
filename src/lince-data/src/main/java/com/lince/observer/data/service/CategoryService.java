package com.lince.observer.data.service;

import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.CategoryInformation;
import com.lince.observer.data.bean.categories.Criteria;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import java.util.List;

/**
 * Created by Alberto Soto. 30/3/24
 */
public interface CategoryService {
    List<Criteria> getCriteria();

    List<CategoryData> getChildren(Integer id);

    Pair<Criteria, Category> findDataById(Integer id, String code, String name);

    void pushAll(List<Criteria> data);

    void checkToolIntegrity(List<Criteria> newData);

    void clearAll();

    /**
     * Return category by code or category info is needed
     * @param code valid code
     * @return proper category data or similar with non valid info
     */
    default CategoryData findCategoryByCode(String code) {
        Pair<Criteria,Category> data = findDataById(null, code, null);
        if (data.getValue()!=null){
            return data.getValue();
        }else{
            if(data.getKey().isInformationNode()){
                return new CategoryInformation(data.getKey(), StringUtils.EMPTY);
            }else{
                throw new NullPointerException();
            }
        }
    }

    default List<CategoryData> getCollection() {
        return this.getChildren(0);
    }

    default CategoryData findCategoryById(Integer id) {
        return findDataById(id, null, null).getValue();
    }

    default CategoryData findCategoryByName(String name) {
        return findDataById(null, null, name).getValue();
    }

}
