package com.lince.observer.data.bean;

/**
 * com.lince.observer.data.bean
 * Class KeyValue
 * 19/03/2019
 *
 * @author berto (alberto.soto@gmail.com)
 * Replaces Tuple for JSON Response using Spring object2json serialization
 */
public class KeyValue<A, B> {
    public A key;
    public B value;

    public KeyValue() {

    }

    public KeyValue(A a, B b) {
        this.key = a;
        this.value = b;
    }

    public A getKey() {
        return key;
    }

    public void setKey(A key) {
        this.key = key;
    }

    public B getValue() {
        return value;
    }

    public void setValue(B value) {
        this.value = value;
    }
}
