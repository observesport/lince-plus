package com.deicos.lince.app.base.common;

import java.io.Serializable;
import java.util.HashMap;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.data
 * @author berto (alberto.soto@gmail.com)in 11/02/2016.
 * Description:
 */
public class EnumStructure implements Serializable {

    private CgEnum[] enumItems;

    public CgEnum[] getEnumData() {
        return enumItems;
    }

    public void setEnumData(CgEnum[] events) {
        this.enumItems = events;
    }

    /**
     * Devuelve el objeto necesario para rendering
     *
     * @return
     */
    public HashMap<String, String> getDataValues() {
        HashMap<String, String> rtn = new HashMap<>();
        for (CgEnum elem : enumItems) {
            rtn.put(elem.getItemValue(), elem.getItemLabel());
        }
        return rtn;
    }
}
