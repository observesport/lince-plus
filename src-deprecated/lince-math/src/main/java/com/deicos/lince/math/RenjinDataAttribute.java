package com.deicos.lince.math;

import com.deicos.lince.data.CgEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Asoto
 * Date: 03/09/2014 10:47
 * Description:  Data session attribute constants
 * <p/>
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RenjinDataAttribute implements CgEnum {
    CONTEXT("ctx"),
    DATA_MATRIX("linceData"),
    DATA_MATRIX_BY_CATEGORY("linceDataByCategory"),;

    private final String parameterName;

    /**
     * @param newValue
     */
    private RenjinDataAttribute(final String newValue) {
        this.parameterName = newValue;
    }

    public String getItemLabel() {
        return parameterName;
    }

    public String getItemValue() {
        return this.toString();
    }

    public static RenjinDataAttribute castString(String key) {
        try {
            return RenjinDataAttribute.valueOf(key.toUpperCase());
        } catch (Exception e) {
            return CONTEXT;
        }
    }

    public static List<String> getLabels(){
        List<String> rtn = new ArrayList<>();
        for (RenjinDataAttribute item: RenjinDataAttribute.values()) {
            rtn.add(item.getItemLabel());
        }
        return rtn;
    }
}
