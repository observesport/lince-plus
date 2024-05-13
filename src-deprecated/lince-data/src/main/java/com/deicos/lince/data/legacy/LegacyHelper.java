package com.deicos.lince.data.legacy;

import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.Criteria;
import lince.modelo.InstrumentoObservacional.InstrumentoObservacional;
import lince.modelo.Registro;

import java.util.List;

/**
 * lince-scientific-base
 * com.deicos.lince.data
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
