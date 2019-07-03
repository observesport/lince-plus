package com.deicos.lince.math.service;

import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.Category;
import com.deicos.lince.data.bean.categories.CategoryData;
import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.data.bean.wrapper.LinceRegisterWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * lince-scientific-desktop
 * com.deicos.lince.app.service
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 */
@Service
public class CategoryService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    //private static List<Criteria> criteria = new LinkedList<>();

    @Autowired
    protected DataHubService dataHubService;

    public void generateDummyCollection() {
        CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(dataHubService.getCriteria()));
        catService.generateDummyCollection();
        dataHubService.getCriteria().setAll(catService.getLinkedList());
    }

    public List<CategoryData> getCollection() {
        return this.getChildren(0);
    }

    public List<Criteria> getCriteria() {
        return dataHubService.getCriteria();
    }

    public List<CategoryData> getChildren(Integer id) {
        CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(dataHubService.getCriteria()));
        return catService.getChildren(id);
    }

    public Pair<Criteria, Category> findDataById(Integer id, String code, String name) {
        try {
            CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(dataHubService.getCriteria()));
            return catService.findTreeEntryById(id, code, name);
        } catch (Exception e) {
            log.error("finding criteria by id", e);
        }
        return null;
    }

    public CategoryData findCategoryById(Integer id) {
        return findDataById(id, null, null).getValue();
    }

    public CategoryData findCategoryByCode(String code) {
        return findDataById(null, code, null).getValue();
    }

    public CategoryData findCategoryByName(String name) {
        return findDataById(null, null, name).getValue();
    }


    /**
     * Pushes the current visualization tool into the register
     *
     * @param data Custom data collection
     */
    public void pushAll(List<Criteria> data) {
        checkToolIntegrity(data);
        clearAll();
        CollectionUtils.addAll(dataHubService.getCriteria(), data.iterator());
        CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(dataHubService.getCriteria()));
        catService.generateID();
        //if (generateReduceValues){
        catService.generateReductionData();
        catService.generateCodesFromReductionData();
        //}
        dataHubService.getCriteria().setAll(catService.getLinkedList());
    }

    /**
     * Recorre toda la herramienta y borra los elementos que no aparece en la nueva seleccion
     * Utiliza el método equals sobreescrito, para no tener que tener en cuenta el id.
     * @param newData nueva herramienta de visualización
     */
    private void checkToolIntegrity(List<Criteria> newData) {
        //Generamos una lista de criterios y categorias que tenemos anteriormente con un contador de uso
        HashMap<CategoryData, Integer> countTable = new HashMap<>();
        for (Criteria cri : dataHubService.getCriteria()) {
            countTable.put(cri, 0);
            for (Category cat : cri.getInnerCategories()) {
                countTable.put(cat, 0);
            }
        }
        //Checkeamos cuantas veces surgen en la nueva lista
        for (Criteria cri : newData) {
            countTable.forEach((k, v) -> {
                if (k.equals(cri)) {
                    countTable.replace(k,v+1);
                }
            });
            for (Category cat : cri.getInnerCategories()) {
                countTable.forEach((k, v) -> {
                    if (k.equals(cat)) {
                        countTable.replace(k,v+1);
                    }
                });
            }
        }
        //Borramos los registros que tengan contador 0 para criterio o categoría
        for (LinceRegisterWrapper row : dataHubService.getDataRegister()) {
            for(RegisterItem reg: row.getRegisterData()){
                //para borrar se debe hacer con iterator
                for (Iterator it = reg.getRegister().iterator(); it.hasNext();) {
                    Category cat= (Category) it.next();
                    countTable.forEach((k, v) -> {
                        if (k.equals(cat) && v==0) {
                            it.remove();
                        }
                    });
                }
            }
        }

    }

    public void clearAll() {
        dataHubService.getCriteria().clear();
    }

}
