package com.lince.observer.data.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.data.generic
 * @author berto (alberto.soto@gmail.com)in 11/02/2016.
 * Description:
 * Helper class to valid JSON responses, wrapping all data in an array and being fully functional
 * It also is the base data container
 */
public class DataTableWrapper<T> {
    List<T> data;


    public DataTableWrapper() {
        this(new ArrayList<T>());
    }

    public DataTableWrapper(List<T> t) {
        setData(t);
    }

    public DataTableWrapper(T t){
        this();
        data.add(t);
    }

    /**
     * @return the persons
     */
    public List<T> getData() {
        return data;
    }

    /**
     * @param persons the persons to set
     */
    public void setData(List<T> persons) {
        this.data = persons;
    }
}
