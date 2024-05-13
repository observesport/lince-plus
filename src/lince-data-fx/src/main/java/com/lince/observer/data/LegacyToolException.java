package com.lince.observer.data;

import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;

/**
 * com.lince.observer.data
 * Class ToolException
 * 20/02/2020
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class LegacyToolException extends Exception {
    private String message;
    private Criterio criterio;
    private Categoria categoria;

    public LegacyToolException(String message){
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

    public void setMessage(String message) {
        this.message = message;
    }

    public Criterio getCriterio() {
        return criterio;
    }

    public void setCriterio(Criterio criterio) {
        this.criterio = criterio;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
