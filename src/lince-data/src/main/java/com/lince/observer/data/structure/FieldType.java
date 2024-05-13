package com.lince.observer.data.structure;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lince.observer.data.CgEnum;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.data.data
 * @author berto (alberto.soto@gmail.com)in 11/02/2016.
 * Description:
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Deprecated
public enum FieldType implements CgEnum {
    HIDDEN("hidden"),
    HTML("html"),
    JSON("json"),
    STRING("string"),
    NUMBER("number"),
    DATE("date"),
    OPTION_LIST("optionList");

    private final String parameterName;

    public String getItemLabel() {
        return parameterName;
    }

    public String getItemValue() {
        return this.toString();
    }

    /**
     * @param newValue
     */
    private FieldType(final String newValue) {
        this.parameterName = newValue;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
  /*  @Override
    public String toString() {
        return parameterName;
    }
*/
}
