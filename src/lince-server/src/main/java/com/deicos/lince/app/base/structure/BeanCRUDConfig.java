package com.deicos.lince.app.base.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.data.structure
 * @author berto (alberto.soto@gmail.com)in 19/02/2016.
 * Description:
 */
public class BeanCRUDConfig {

    List<BeanActionsConfig> actions = new ArrayList<>();
    List<BeanFormConfig> fields = new ArrayList<>();

    public static final String ACTION_TYPE_CELL = "cell";
    public static final String ACTION_TYPE_BUTTON = "button";
    public static final String ACTION_TYPE_TABLE = "table";


    public static final String ICON_FIND=   "glyphicon glyphicon-share";
    public static final String ICON_EDIT=   "glyphicon glyphicon-pencil";
    public static final String ICON_DELETE= "glyphicon glyphicon-trash";
    public static final String ICON_ADD=    "glyphicon glyphicon-plus";


    public List<BeanActionsConfig> getActions() {
        return actions;
    }

    public void setActions(List<BeanActionsConfig> actions) {
        this.actions = actions;
    }

    public List<BeanFormConfig> getFields() {
        return fields;
    }

    public void setFields(List<BeanFormConfig> fields) {
        this.fields = fields;
    }


    public void addAction(String uriTemplate,String actionType,String style, FieldActionType actionLinkType){
        BeanActionsConfig bean = new BeanActionsConfig();
        bean.setUriTemplate(uriTemplate);
        bean.setStyle(style);
        bean.setType(actionType);
        bean.setLinkType(actionLinkType.toString());
        actions.add(bean);
    }

    public void addField(int order,String name,String fieldType,String validationType){
        BeanFormConfig bean = new BeanFormConfig();
        bean.setName(name);
        bean.setType(fieldType);
        bean.setOrder(order);
        bean.setValidation(validationType);
    }
}
