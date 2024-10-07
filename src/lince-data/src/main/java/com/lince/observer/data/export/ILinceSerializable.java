package com.lince.observer.data.export;
/**
 * lince-scientific-desktop
 * com.lince.observer.data.export
 * @author berto (alberto.soto@gmail.com)in 05/07/2016.
 * Description:
 *
 */
interface ILinceSerializable {
    String doLinceSerialization();
    void doLinceImport(String data);
}