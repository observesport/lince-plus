package com.deicos.lince.app.base.structure;

import com.deicos.lince.app.base.common.CgEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.data.data
 * @author berto (alberto.soto@gmail.com)in 11/02/2016.
 * Description:
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FieldActionType implements CgEnum {
    AJAX("ajax"),
    SEND("send"),
    FORM("form");

    private final String parameterName;

    /**
     * @param newValue
     */
    private FieldActionType(final String newValue) {
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
    public String getItemLabel() {
        return parameterName;
    }

    public String getItemValue() {
        return this.toString();
    }
}
