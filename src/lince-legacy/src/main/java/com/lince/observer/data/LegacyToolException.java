package com.lince.observer.data;

import com.lince.observer.legacy.instrumentoObservacional.Categoria;

/**
 * com.lince.observer.data
 * Class ToolException
 * 20/02/2020
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class LegacyToolException extends Exception {
    private String message;
    private Categoria categoria;

    public LegacyToolException(String message){
        this.message=message;
    }

    public LegacyToolException(String message, Throwable cause) {
        super(message, cause);
        this.message=message;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
