package com.lince.observer.math.service;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.CategoryInformation;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.service.CategoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * lince-scientific-desktop
 * .app.service
 *
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final DataHubService dataHubService;

    @Autowired
    public CategoryServiceImpl(DataHubService dataHubService) {
        this.dataHubService = dataHubService;
    }

    public void generateDummyCollection() {
        CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(dataHubService.getCriteria()));
        catService.generateDummyCollection();
        dataHubService.getCriteria().setAll(catService.getLinkedList());
    }

    @Override
    public List<CategoryData> getCollection() {
        return this.getChildren(0);
    }

    @Override
    public List<Criteria> getCriteria() {
        return dataHubService.getCriteria();
    }

    @Override
    public List<CategoryData> getChildren(Integer id) {
        CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(dataHubService.getCriteria()));
        return catService.getChildren(id);
    }

    @Override
    public Pair<Criteria, Category> findDataById(Integer id, String code, String name) {
        try {
            CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(dataHubService.getCriteria()));
            return catService.findTreeEntryById(id, code, name);
        } catch (Exception e) {
            log.error("finding criteria by id", e);
        }
        return null;
    }

    @Override
    public CategoryData findCategoryById(Integer id) {
        return findDataById(id, null, null).getValue();
    }

    /**
     * Return category by code or category info is needed
     * @param code valid code
     * @return proper category data or similar with non valid info
     */
    @Override
    public CategoryData findCategoryByCode(String code) {
        Pair<Criteria,Category> data = findDataById(null, code, null);
        if (data.getValue()!=null){
            return data.getValue();
        }else{
            if(data.getKey().isInformationNode()){
                return new CategoryInformation(data.getKey(),StringUtils.EMPTY);
            }else{
                log.error("Invalid concept");
                throw new NullPointerException();
            }
        }

    }

    @Override
    public CategoryData findCategoryByName(String name) {
        return findDataById(null, null, name).getValue();
    }


    /**
     * Pushes the current visualization tool into the register
     *
     * @param data Custom data collection
     */
    @Override
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
     *
     * @param newData nueva herramienta de visualización
     */
    @Override
    public void checkToolIntegrity(List<Criteria> newData) {
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
                    countTable.replace(k, v + 1);
                }
            });
            for (Category cat : cri.getInnerCategories()) {
                countTable.forEach((k, v) -> {
                    if (k.equals(cat)) {
                        countTable.replace(k, v + 1);
                    }
                });
            }
        }
        //Borramos los registros que tengan contador 0 para criterio o categoría
        for (LinceRegisterWrapper row : dataHubService.getDataRegister()) {
            for (RegisterItem reg : row.getRegisterData()) {
                //para borrar se debe hacer con iterator
                for (Iterator it = reg.getRegister().iterator(); it.hasNext(); ) {
                    Category cat = (Category) it.next();
                    countTable.forEach((k, v) -> {
                        if (k.equals(cat) && v == 0) {
                            it.remove();
                        }
                    });
                }
            }
        }

    }

    @Override
    public void clearAll() {
        dataHubService.getCriteria().clear();
    }

}