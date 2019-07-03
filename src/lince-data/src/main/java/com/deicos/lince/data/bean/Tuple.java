package com.deicos.lince.data.bean;


import java.io.Serializable;

/**
 * com.deicos.lince.data.bean
 * Class Tuple
 * @author berto (alberto.soto@gmail.com). 28/04/2018
 * @see http://www.javatuples.org/ for id more thorough approach
 * @see KeyValue to json response with identical behaviour
 * IMPORTANT! This class cant be used for Rest responses, does not serialize properly
 *
 */
public class Tuple<A, B> implements Serializable{

    public final A key;
    public final B value;

    public Tuple(A key, B b) {
        this.key = key;
        this.value = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        if (!key.equals(tuple.key)) return false;
        return value.equals(tuple.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}