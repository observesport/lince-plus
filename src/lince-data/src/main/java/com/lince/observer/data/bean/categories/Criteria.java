package com.lince.observer.data.bean.categories;

import com.lince.observer.data.LinceDataConstants;

import java.util.LinkedList;

/**
 * lince-scientific-desktop
 * com.lince.observer.data.bean.categories
 *
 * @author berto (alberto.soto@gmail.com)in 02/03/2016.
 * Description:
 */
public class Criteria extends CategoryData /*implements INodeInfo*/{
    private boolean persistence;
    private boolean informationNode=false;
    LinkedList<Category> innerCategories = new LinkedList<>();

    public Criteria(Integer id, String name) {
        super(id, name, 0);
    }

    public Criteria() {
        super();
    }

    @Override
    public boolean isCategory() {
        return false;
    }

    @Override
    public String getCategoryDataPrefix() {
        return LinceDataConstants.CRITERIA_PREFIX;
    }


    public LinkedList<Category> getInnerCategories() {
        return innerCategories;
    }

    public void setInnerCategories(LinkedList<Category> innerCategories) {
        this.innerCategories = innerCategories;
    }

    public boolean isPersistence() {
        return persistence;
    }

    public void setPersistence(boolean persistence) {
        this.persistence = persistence;
    }


    // TODO check what is this...REALLY. WTF
    public static Criteria getRecoveryCriteria(){
        Criteria aux = new Criteria();
        aux.setCode("RECOVERY");
        aux.setName("Data recovery criteria from legacy data");
        aux.setId(1234567890);
        aux.setInformationNode(false);
        aux.setPersistence(false);
        return aux;
    }

    public boolean isInformationNode() {
        return informationNode;
    }

    public void setInformationNode(boolean informationNode) {
        this.informationNode = informationNode;
    }
}
