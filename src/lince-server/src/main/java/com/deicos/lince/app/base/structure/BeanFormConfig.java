package com.deicos.lince.app.base.structure;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.data.structure
 * @author berto (alberto.soto@gmail.com)in 19/02/2016.
 * Description:
 */
public class BeanFormConfig {

    int order;
    String name;
    String type;
    String validation;
    String uriData;
    String label;

    public BeanFormConfig() {
    }

    /**
     * @param order
     * @param name
     * @param type
     * @param validation
     */
    public BeanFormConfig(int order, String name,String label, String type, String validation) {
        this.order = order;
        this.name = name;
        this.label= label;
        this.type = type;
        this.validation = validation;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {

        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getUriData() {
        return uriData;
    }

    public void setUriData(String uriData) {
        this.uriData = uriData;
    }
}
