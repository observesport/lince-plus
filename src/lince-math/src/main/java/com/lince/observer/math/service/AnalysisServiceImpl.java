package com.lince.observer.math.service;

import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.CategoryInformation;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.highcharts.HighChartsWrapper;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.AnalysisServiceBase;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.data.service.ProfileService;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * lince-scientific-desktop
 * .app.service
 *
 * @author berto (alberto.soto@gmail.com)in 22/06/2016.
 * Description:
 */
@Service
@DesktopQualifier
public class AnalysisServiceImpl extends AnalysisServiceBase implements AnalysisService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final CategoryService categoryService;
    private final DataHubService dataHubService;

    @Autowired
    public AnalysisServiceImpl(@DesktopQualifier CategoryService categoryService, @DesktopQualifier ProfileService profileService, DataHubService dataHubService) {
        super(categoryService, profileService);
        this.categoryService = categoryService;
        this.dataHubService = dataHubService;
    }

    @Override
    public boolean deleteObservationById(Integer id) {
        return deleteRegisterById(id, dataHubService.getCurrentDataRegister());
    }

    @Override
    public boolean deleteObservationByMoment(Double moment) {
        return deleteMomentInfo(moment, dataHubService.getCurrentDataRegister());
    }

    @Override
    public List<RegisterItem> getAllObservations() {
        ensureDataRegisterConsistency();
        return dataHubService.getCurrentDataRegister();
    }

    @Override
    public List<RegisterItem> getObservationById(UUID uuid) {
        try {
            ensureDataRegisterConsistency();
            return dataHubService.getRegisterById(uuid).getRegisterData();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Pair<CategoryData, Double>> getObservationRegisterVisibility(List<RegisterItem> register) {
        return getAllRegisterVisibility(register, dataHubService.getCriteria());
    }

    @Override
    public HighChartsWrapper getRegisterStatsByCategory() {
        List<RegisterItem> register = getSortedObservations();
        List<Criteria> tool = dataHubService.getCriteria();
        return getRegisterStatsByCategory(register, tool);
    }

    @Override
    public boolean saveObservation(Double videoTime, Category... categories) {
        return pushRegister(dataHubService.getCurrentDataRegister(), videoTime, categories);
    }

    @Override
    public boolean saveObservation(RegisterItem item) {
        return pushRegister(dataHubService.getCurrentDataRegister(), item);
    }

    /***
     * Comprueba que para cada elemento del registro existe uno asociado similar en el instrumento de observación
     * y sólo inserta los criterios validados.
     *
     * Así evitamos inconsistencia en el registro para observaciones que ya no existen en el instrumento de observación.
     * Aumenta el computo en las consultas, pero evita inconsistencias.
     *
     *
     */
    private void ensureDataRegisterConsistency() {
        for (RegisterItem reg : dataHubService.getCurrentDataRegister()) {
            int i = 0;
            for (Category c : reg.getRegister()) {
                Pair<Criteria, Category> elem = categoryService.findToolEntryByIdentifier(null, c.getCode(), null);
                if (elem != null) {
                    Category tableValue;
                    if (elem.getValue() != null) {
                        tableValue = elem.getValue();
                    } else {
                        tableValue = new CategoryInformation(elem.getKey(), c.getNodeInformation());
                    }
                    reg.getRegister().set(i, tableValue);
                    i++;
                }

            }
        }
    }


}
