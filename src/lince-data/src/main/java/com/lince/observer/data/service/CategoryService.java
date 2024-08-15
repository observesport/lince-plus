package com.lince.observer.data.service;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.CategoryInformation;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Alberto Soto. 30/3/24
 */
public interface CategoryService {
    List<Criteria> getObservationTool();

    List<CategoryData> getCategoriesByParent(Integer id);

    @Deprecated
    Pair<Criteria, Category> findToolEntryByIdentifier(Integer id, String code, String name);

    Pair<Criteria, Category> findToolEntryByIdentifier(List<Criteria> criteriaList, Integer id, String code, String name);

    void saveObservationTool(List<Criteria> data);

    /**
     * Recorre toda la herramienta y borra los elementos que no aparecen en la nueva seleccion
     * Utiliza el método equals sobreescrito, para no tener que tener en cuenta el id.
     *
     * @param newObservationTool nueva herramienta de visualización
     * @param register           observed Register
     */
    default void checkObservationToolIntegrityWithRegister(List<Criteria> savedObservationTool, List<Criteria> newObservationTool, List<LinceRegisterWrapper> register) {
        //Generamos una lista de criterios y categorias que tenemos anteriormente con un contador de uso
        HashMap<CategoryData, Integer> countTable = new HashMap<>();
        for (Criteria cri : savedObservationTool) {
            countTable.put(cri, 0);
            for (Category cat : cri.getInnerCategories()) {
                countTable.put(cat, 0);
            }
        }
        //Checkeamos cuantas veces surgen en la nueva lista
        for (Criteria cri : newObservationTool) {
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
        for (LinceRegisterWrapper row : register) {
            for (RegisterItem reg : row.getRegisterData()) {
                //para borrar se debe hacer con iterator
                for (Iterator<Category> it = reg.getRegister().iterator(); it.hasNext(); ) {
                    Category cat = it.next();
                    countTable.forEach((k, v) -> {
                        if (k.equals(cat) && v == 0) {
                            it.remove();
                        }
                    });
                }
            }
        }
    }

    default void clearSelectedObservationTool() {
    }

    ;

    /**
     * Return category by code or category info is needed
     *
     * @param code valid code
     * @return proper category data or similar with non-valid info
     */
    default CategoryData findCategoryByCode(String code) {
        Pair<Criteria, Category> data = findToolEntryByIdentifier(null, code, null);
        if (data.getValue() != null) {
            return data.getValue();
        } else {
            if (data.getKey().isInformationNode()) {
                return new CategoryInformation(data.getKey(), StringUtils.EMPTY);
            } else {
                throw new NullPointerException();
            }
        }
    }

    @Deprecated
    default List<CategoryData> getCollection() {
        return this.getCategoriesByParent(0);
    }

    default CategoryData findCategoryById(Integer id) {
        return findToolEntryByIdentifier(id, null, null).getValue();
    }

    default CategoryData findCategoryByName(String name) {
        return findToolEntryByIdentifier(null, null, name).getValue();
    }

}
