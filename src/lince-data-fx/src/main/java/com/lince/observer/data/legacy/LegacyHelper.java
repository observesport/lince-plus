package com.lince.observer.data.legacy;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.Registro;

import java.util.List;

/**
 * lince-scientific-base
 * com.lince.observer.data
 * Created by Alberto Soto Fernandez in 07/07/2017.
 * Description:
 */
public class LegacyHelper {

    public List<RegisterItem> getDataRegisterCompatible(){
        Registro r = Registro.getInstance();
        return null;
    }

    public List<Criteria> getCompatibleCriteria(){
        InstrumentoObservacional i = InstrumentoObservacional.getInstance();
        return null;
    }

    public void setLegacyData(){

    }

}
