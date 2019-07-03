package com.deicos.lince.data.export;
/**
 * lince-scientific-desktop
 * com.deicos.lince.data.export
 * @author berto (alberto.soto@gmail.com)in 05/07/2016.
 * Description:
 *
 */
interface ILinceSerializable {
    String doLinceSerialization();
    void doLinceImport(String data);
}