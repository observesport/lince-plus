package com.deicos.lince.math.service;

import com.deicos.lince.data.bean.categories.Category;
import com.deicos.lince.data.bean.categories.CategoryData;
import com.deicos.lince.data.bean.categories.Criteria;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * lince-scientific-desktop
 * com.deicos.lince.app.service
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 *
 * Old way service for tool settings.
 * Everything should be move to CategoryService
 */
@Deprecated
@Service
public class CategoryServiceOld {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private LinkedList<Criteria> criteria;
    private static AtomicInteger idGenerator = new AtomicInteger();

    /**
     * Mandatory to allow service creation
     */
    public CategoryServiceOld() {
        this(new LinkedList<>());
    }

    public CategoryServiceOld(LinkedList<Criteria> data) {
        this.criteria = data;
    }

    /**
     * Generates next valid ID
     *
     * @return next id in collection
     */
    public Integer generateID() {
        //analizamos todos los ids y devolvemos el maximo
        try {
            for (Criteria value : criteria) {
                Integer currentGroupID = value.getId() == null ? -1 : value.getId();
                if (value.getId() == null) {
                    value.setId(idGenerator.incrementAndGet()); //corregimos posible error de ids al recorrer
                    generateID();
                }else{
                    if (currentGroupID > idGenerator.get()) {
                        idGenerator.set(currentGroupID);
                    }
                }
                for (Category cat : value.getInnerCategories()) {
                    if (cat.getId() == null) {
                        cat.setId(idGenerator.incrementAndGet());
                    }else{
                        if (cat.getId() > idGenerator.get()) {
                            idGenerator.set(cat.getId());
                        }
                    }
                    if (cat.getParent() == null) {
                        cat.setParent(value.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("generateId", e);
        }
        return idGenerator.incrementAndGet();
    }

    /**
     * Generates all custom values for reducing data in stats
     */
    public void generateReductionData() {
        try {
            Integer reductionValue = 0;
            for (Criteria criteriaItem : criteria) {
                criteriaItem.setValue(reductionValue*10);
                Integer categoryValue = criteriaItem.getValue()+1;
                for (Category categoryItem : criteriaItem.getInnerCategories()) {
                    categoryItem.setValue(categoryValue);
                    categoryValue++;
                }
                if(criteriaItem.getInnerCategories().size()>9){
                    reductionValue=Math.round(criteriaItem.getInnerCategories().size()/10);
                }
                reductionValue++;
            }
        } catch (Exception e) {
            log.error("generateId", e);
        }
    }

    public void generateCodesFromReductionData(){
        try {
            Consumer<CategoryData> setCodes = (cat) -> {
                if (cat.getValue()!=null){
                    String parent=StringUtils.EMPTY;
                    if(cat.isCategory()){
                        parent=findById(criteria,((Category)cat).getParent()).getValue()+"-";
                    }
                    String currentCode =String.format("%s%s%s"
                            ,cat.getCategoryDataPrefix()
                            ,parent
                            ,cat.getValue());
                    if (StringUtils.isEmpty(cat.getCode())){
                        cat.setCode(currentCode.toUpperCase());
                    }
                    if (StringUtils.isEmpty(cat.getName())){
                        cat.setName(currentCode.toLowerCase());
                    }
                }
            };
            for (Criteria criteriaItem : criteria) {
                setCodes.accept(criteriaItem);
                for (Category categoryItem : criteriaItem.getInnerCategories()) {
                    setCodes.accept(categoryItem);
                }
            }
        } catch (Exception e) {
            log.error("generateId", e);
        }
    }

    public void generateDummyCollection() {
        CategoryData c1 = pushCategory("C1", null, null);
        pushCriteria(c1.getId(), "C1-1", null, null);
        pushCriteria(c1.getId(), "C1-2", null, null);
        CategoryData c2 = pushCategory("C2", null, null);
        pushCriteria(c2.getId(), "C2-1", null, null);
        pushCriteria(c2.getId(), "C2-2", null, null);
    }

    /**
     * Returns all loaded data
     *
     * @return collection from root
     */
    public List<CategoryData> getCollection() {
        return this.getChildren(0);
    }

    public LinkedList<Criteria> getLinkedList() {
        return criteria;
    }

    /**
     * Returns loaded data by element ID
     *
     * @param id nodeID
     * @return children collection for a category
     */
    public List<CategoryData> getChildren(Integer id) {
        List<CategoryData> rtn = new ArrayList<>();
        if (id == 0) {
            //devuelve los Category Data a nivel de criterio
            return IteratorUtils.toList(criteria.iterator());
        } else {
            //devuelve los Category Data a nivel de categoria
            for (Criteria entry : criteria) {
                if (entry.getId() == id) return IteratorUtils.toList(entry.getInnerCategories().iterator());
            }
        }
        return rtn;
    }

    /**
     * Finds node by id in a linked list
     *
     * @param data collection
     * @param id   node ID
     * @param <T>  current Category
     * @return
     */
    public <T extends CategoryData> T findById(LinkedList<T> data, Integer id) {
        for (T entry : data) {
            if (entry.getId().intValue() == id.intValue())
                return entry;
        }
        return null;
    }

    /**
     * same than findById but return element's position
     *
     * @param data collection
     * @param id   node ID
     * @param <T>  position in tree
     * @return
     */
    public <T extends CategoryData> Integer findPositionById(LinkedList<T> data, Integer id) {
        try {
            if (id != null) {
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getId().intValue() == id.intValue()) {
                        return i;
                    }
                }
            }
        } catch (Exception e) {
            log.error("CatService:findPositionById", e);
        }

        return null;
    }

    public boolean isSameEntry(CategoryData elem, Integer id, String code, String name) {
        if (id != null && elem.getId() == id) {
            return true;
        } else if (StringUtils.isNotEmpty(code) && StringUtils.equals(code, elem.getCode())) {
            return true;
        }
        //last case by exact name
        return (StringUtils.isNotEmpty(name) && StringUtils.equals(name, elem.getName()));
    }

    /**
     * Returns category or category + criteria searching element by id
     *
     * @param id node ID
     * @return element estructure
     */
    public Pair<Criteria, Category> findTreeEntryById(Integer id, String code, String name) {
        Pair<Criteria, Category> elem = null;
        try {
            for (Criteria value : criteria) {
                Integer currentGroupID = value.getId();
                if (isSameEntry(value, id, code, name)) {
                    //if (currentGroupID == id || (code != null && StringUtils.equals(code, value.getCode()))) {
                    elem = new Pair<>(value, null);
                }
                //idGenerator.set(cat.getId());
                for (Category entry : value.getInnerCategories()) {
                    if (isSameEntry(entry, id, code, name)) {
                        //if (entry.getId() == id || (code != null && StringUtils.equals(code, entry.getCode()))) {
                        elem = new Pair<>(value, entry);
                    }
                }
            /* TODO: revisar functional en 1.8!!!
            value.getInnerCategories().stream().filter(cat -> cat.getId() == id).forEach(cat -> {
                //idGenerator.set(cat.getId());
                elem = new Pair<Criteria,Category>(value,cat);
            });*/
            }
        } catch (Exception e) {
            log.error("catService;findTreeEntryById", e);
        }
        return elem;
    }

    /**
     * Pasar a clase de utilidad!
     *
     * @param data
     * @param cat
     * @param position
     * @param related
     * @param <T>
     * @return
     */
    private <T extends CategoryData> CategoryData pushItem(LinkedList<T> data, T cat, String position, Integer related) {
        try {
            if (related == null) {
                data.addLast(cat);
                return data.getLast();
            }
            Integer relatedPosition = findPositionById(data, related);
            switch (position) {
                case CategoryData.POSITION_BEFORE:
                    if (relatedPosition != null) {
                        data.add(relatedPosition, cat);
                        return data.get(relatedPosition);
                    }
                case CategoryData.POSITION_AFTER:
                    if (relatedPosition != null) {
                        data.add(relatedPosition + 1, cat);
                        return data.get(relatedPosition + 1);
                    }
                case CategoryData.POSITION_INNER_FIRST:
                    data.addFirst(cat);
                    return data.getFirst();
                case CategoryData.POSITION_INNER_LAST:
                    data.addLast(cat);
                    return data.getLast();
                default:
                    data.addLast(cat);
                    return data.getLast();
            }
        } catch (Exception e) {
            log.error("catService:pushItem", e);
            return null;
        }
    }


    public CategoryData pushCriteria(Integer parent, String name, String position, Integer related) {
        return pushCriteria(null, parent, name, position, related);
    }

    public CategoryData pushCriteria(Integer id, Integer parent, String name, String position, Integer related) {
        Criteria aux = findById(criteria, parent);//criteria.get(parent);
        if (aux != null) {
            Integer currentID = null;
            if (id == null) {
                currentID = generateID();
            }
            Category cat = new Category(currentID, name, parent);
            return pushItem(aux.getInnerCategories(), cat, position, related);
        }
        return null;
    }

    /**
     * @param name     category name
     * @param position position Type
     * @param related  related category / position to do action
     * @return new category
     */
    public CategoryData pushCategory(String name, String position, Integer related) {
        return pushCategory(null, name, position, related);
    }

    public CategoryData pushCategory(Integer id, String name, String position, Integer related) {
        Integer currentID = null;
        if (id == null) {
            currentID = generateID();
        }
        Criteria newCriteria = new Criteria(id == null ? currentID : id, name);
        return pushItem(criteria, newCriteria, position, related);
    }

    /**
     * Updates an element in the collection
     *
     * @param id            id to search
     * @param configuration new element to replace
     * @return descendant collection
     */
    public CategoryData update(Integer id, CategoryData configuration) {
        try {
            Pair<Criteria, Category> aux = findTreeEntryById(id, null, null);
            if (configuration.getId() == null || StringUtils.isEmpty(configuration.getCode())) {
                //it seeems that configuration:id is not set, let's replace it for the original one
                configuration.setId(id);
            }
            if (aux != null) {
                if (aux.getValue() == null) {
                    // se trata de una categoria
                    BeanUtils.copyProperties(configuration, aux.getKey());
                    criteria.set(findPositionById(criteria, id), aux.getKey());
                    return aux.getKey();
                } else {
                    //se trata de un criterio
                    BeanUtils.copyProperties(configuration, aux.getValue());
                    aux.getKey().getInnerCategories().set(findPositionById(aux.getKey().getInnerCategories(), id), aux.getValue());
                    return aux.getValue();
                }
            }
        } catch (Exception e) {
            log.error("categoryUpdate", e);
        }
        return null;
    }

    /**
     * Deletes an element in the collection all it's descendants
     *
     * @param id            element ID
     * @param configuration not used yet @deprecated
     * @return element from previous or root element after deleting node
     */
    public List<CategoryData> delete(Integer id, CategoryData configuration) {
        Pair<Criteria, Category> aux = findTreeEntryById(id, null, null);
        if (aux != null) {
            Integer position = null;
            if (aux.getValue() == null) {
                position = findPositionById(criteria, id);
                criteria.remove(position.intValue());
                return IteratorUtils.toList(criteria.iterator());
            } else {
                position = findPositionById(aux.getKey().getInnerCategories(), id);
                aux.getKey().getInnerCategories().remove(position.intValue());
                return IteratorUtils.toList(aux.getKey().getInnerCategories().iterator());
            }
        }
        return null;
    }

    public CategoryData move(Integer id, CategoryData configuration) {
        Pair<Criteria, Category> aux = findTreeEntryById(id, null, null);
        if (aux.getKey() != null && configuration.getRelated() != null && StringUtils.isNotEmpty(configuration.getPosition())) {
            //Se ha encontrado el elemento y la configuración del movimiento
            Pair<Criteria, Category> related = findTreeEntryById(configuration.getRelated(), null, null);
            //ojo con los cambios de id
            if (aux.getValue() == null) {
                //es categoria: buscamos todos sus descendientes para insertarlos otra vez
                List<CategoryData> childItems = getChildren(id);
                this.delete(id, null);
                pushCategory(id, aux.getKey().getName(), configuration.getPosition(), configuration.getRelated());
                for (CategoryData item : childItems) {
                    pushCriteria(id, item.getId(), item.getName(), item.getPosition(), item.getRelated());
                }
            } else {
                //es criterio
                this.delete(id, null);
                pushCriteria(id, aux.getValue().getParent(), aux.getValue().getName(), configuration.getPosition(), configuration.getRelated());
            }
        }
        return configuration;

    }

    //return IteratorUtils.toList();
    //get
/*
r:site/nodeChildren
id:0
        {"nodes":[{"id":7228,"name":"Jesus love you!","level":0,"type":"default"}]}
 */
/*
get por id:
r:site/nodeChildren
id:7228
        {"nodes":[{"id":7271,"name":"ana are mere","level":1,"type":"default"},{"id":7273,"name":"11","level":1,"type":"default"},{"id":7278,"name":"teste","level":1,"type":"default"},{"id":7274,"name":"aaa","level":1,"type":"default"}]}
*/
/*
save:       r:site/nodeCreate
POST
parent:0
name:asd
position:before // position:after //
related:7228
        result:
            {"id":7287,"name":"asd","level":0,"type":"default"}
 */
/*
children    r:site/nodeChildren
id:7288
        {"nodes":[]}
 */
/*
firstchildren
parent:7288
name:asd
position:firstChild
related:7288
        response: {"id":7289,"name":"asd","level":1,"type":"default"}
 */
/*
r:site/nodeMove
id:7289
related:7288
position:lastChild
        {"id":7289,"name":"asd","level":1,"type":"default"}
 */
}
