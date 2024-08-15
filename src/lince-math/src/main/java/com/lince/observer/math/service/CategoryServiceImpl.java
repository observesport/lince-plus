package com.lince.observer.math.service;

import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.service.CategoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
@DesktopQualifier
public class CategoryServiceImpl implements CategoryService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final DataHubService dataHubService;

    @Autowired
    public CategoryServiceImpl(DataHubService dataHubService) {
        this.dataHubService = dataHubService;
    }

    @Override
    public List<Criteria> getObservationTool() {
        return dataHubService.getCriteria();
    }

    @Override
    public List<CategoryData> getCategoriesByParent(Integer id) {
        CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(dataHubService.getCriteria()));
        return catService.getChildren(id);
    }

    @Override
    @Deprecated
    public Pair<Criteria, Category> findToolEntryByIdentifier(Integer id, String code, String name) {
        return findToolEntryByIdentifier(dataHubService.getCriteria(), id, code, name);
    }

    @Override
    public Pair<Criteria, Category> findToolEntryByIdentifier(List<Criteria> criteriaList, Integer id, String code, String name) {
        try {
            CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(criteriaList));
            return catService.findTreeEntryById(id, code, name);
        } catch (Exception e) {
            log.error("finding criteria by id", e);
        }
        return null;
    }

    /**
     * Pushes the current visualization tool into the register
     *
     * @param observationTool Custom observationTool collection
     */
    @Override
    public void saveObservationTool(List<Criteria> observationTool) {
        List<LinceRegisterWrapper> dataRegister = dataHubService.getDataRegister();
        checkObservationToolIntegrityWithRegister(dataHubService.getCriteria(), observationTool, dataRegister);
        clearSelectedObservationTool();
        CollectionUtils.addAll(dataHubService.getCriteria(), observationTool.iterator());
        CategoryServiceOld catService = new CategoryServiceOld(new LinkedList<>(dataHubService.getCriteria()));
        catService.generateID();
        //if (generateReduceValues){
        catService.generateReductionData();
        catService.generateCodesFromReductionData();
        //}
        dataHubService.getCriteria().setAll(catService.getLinkedList());
    }

    @Override
    public void clearSelectedObservationTool() {
        dataHubService.getCriteria().clear();
    }

}
