package com.lince.observer.data.service;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.highcharts.HighChartsSerie;
import com.lince.observer.data.bean.highcharts.HighChartsWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Alberto Soto. 23/5/24
 */
public abstract class AnalysisServiceBase implements AnalysisService {

    protected boolean deleteRegisterById(Integer id, List<RegisterItem> registerList) {
        try {
            if (id != null) {
                RegisterItem selectedRegister = registerList.stream()
                        .filter(p -> p.getId().equals(id))
                        .findFirst()
                        .orElse(null);
                return registerList.remove(selectedRegister);
            }
        } catch (Exception e) {
            log.error("Deleting register by id", e);
        }
        return false;
    }
    protected boolean deleteMomentInfo(Double moment, List<RegisterItem> registerList) {
        boolean isDeleted = false;
        try {
            if (moment != null) {
                for (RegisterItem reg : registerList) {
                    if (reg.getVideoTime().doubleValue() == moment.doubleValue()
                            || convertSysMoment(reg.getVideoTime()).doubleValue() == convertSysMoment(moment).doubleValue()) {
                        registerList.remove(reg);
                        isDeleted = true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("deleteMoment", e);
        }
        return isDeleted;
    }
    protected boolean pushRegister(RegisterItem item, List<RegisterItem> registerList) {
        try {
            Optional<RegisterItem> registerItem = registerList.stream()
                    .filter(p -> p.getId().equals(item.getId()) && NumberUtils.compare(p.getVideoTime().longValue(), item.getVideoTime().longValue()) == 0
                            || NumberUtils.compare(p.getVideoTime().longValue(), item.getVideoTime().longValue()) == 0
                            || (p.getSaveDate() != null && p.getSaveDate().equals(item.getSaveDate())))
                    .findFirst();
            if (registerItem.isPresent()) {
                BeanUtils.copyProperties(item, registerItem.get(), getNullPropertyNames(item));
            } else {
                item.setId(generateID());
                registerList.add(item);
            }
        } catch (ConcurrentModificationException e) {
            log.error("Concurrent modification exception!", e);
            return false;
        }
        return true;
    }

    public boolean pushRegister(List<RegisterItem> registerList, Double videoTime, Category... categories) {
        return pushRegister(new RegisterItem(convertSysMoment(videoTime), categories), registerList);
    }

    abstract protected Integer generateID();

    /**
     * gets null properties values to use with copy bean propertis
     *
     * @param source
     * @return
     */
    protected String[] getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

    protected List<Pair<CategoryData, Double>> getAllRegisterVisibility(List<RegisterItem> register, List<Criteria> tool) {
        List<Pair<CategoryData, Double>> globalCounter = new ArrayList<>();
        try {
            for (Criteria cri : tool) {
                CollectionUtils.addAll(globalCounter, getRegisterVisibility(cri, register));
            }
        } catch (Exception e) {
            log.error("global register count", e);
        }
        return globalCounter;
    }

    protected HighChartsWrapper getRegisterStatsByCategory(List<RegisterItem> register, List<Criteria> tool) {
        HighChartsWrapper rtn = new HighChartsWrapper();
        HighChartsWrapper drillDown = new HighChartsWrapper();
        try {
            //obtenemos datos de contabilización
            List<Pair<CategoryData, Double>> globalCounter = getAllRegisterVisibility(register);
            Double total = getTotals(globalCounter);
            //tenemos las categorias sin agrupar, con sus totales
            //Montamos la lista de los padres
            HighChartsSerie rootSerie = new HighChartsSerie();
            if (total > 0) {
                //si sacan stats sin haber visto tenemos que devolver vacio
                for (Criteria cri : tool) {
                    if (!cri.isInformationNode()) {
                        double totalPerCategory = 0;
                        HighChartsSerie childSerie = new HighChartsSerie();
                        for (Pair<CategoryData, Double> item : globalCounter) {
                            //si es el mismo padre
                            if (item.getKey().getParent().equals(cri.getId())) {
                                totalPerCategory += item.getValue();
                                childSerie.getDataBean().add(getHighChartsBean(item.getKey().getName(), getFrequency(item.getValue(), total), item.getValue()));
                            }
                        }
                        childSerie.setName(cri.getName());
                        double percentPerCategory = getFrequency(totalPerCategory, total);
                        rootSerie.getDataBean().add(getHighChartsBean(cri.getName(), percentPerCategory, totalPerCategory));
                        drillDown.getSeries().add(childSerie);
                    }
                }
            }
            rtn.getDrilldown().add(drillDown);
            rtn.getSeries().add(rootSerie);
        } catch (Exception e) {
            log.error("Error en graficas ", e);
        }
        return rtn;
    }
}
