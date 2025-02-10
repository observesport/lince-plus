package com.lince.observer.data.service;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.highcharts.HighChartsSerieBean;
import com.lince.observer.data.bean.highcharts.HighChartsWrapper;
import com.lince.observer.data.bean.wrapper.SceneWrapper;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Alberto Soto. 14/5/24
 */
public interface AnalysisService {
    Logger log = LoggerFactory.getLogger(AnalysisService.class);

    /**
     * Normaliza el momento de sistema a dos decimales
     *
     * @param moment
     * @return
     */
    default Double convertSysMoment(Double moment) {
        return Math.round(moment * 100.0) / 100.0;
    }

    default Double getFrequency(Double item, Double total) {
        if (item != null && total != null) return (item * 100) / total;
        else return null;
    }

    default List<RegisterItem> getSortedObservations() {
        return getAllObservations().stream().sorted().toList();
    }

    default List<Pair<CategoryData, Double>> getClusteredObservationsByCriteria(Criteria cri, List<RegisterItem> userSceneData) {
        List<Pair<CategoryData, Double>> rtnValues = new ArrayList<>();
        try {
            if (cri != null) {
                for (Category cat : cri.getInnerCategories()) {
                    double counter = 0;
                    for (RegisterItem scene : userSceneData) {
                        for (Category sceneCat : scene.getRegister()) {
                            if (sceneCat.equals(cat)) {
                                counter++;
                            }
                        }
                    }
                    rtnValues.add(new Pair<>(cat, counter));
                }
            }
        } catch (Exception e) {
            log.error("getRegisterVisibility", e);
        }
        return rtnValues;
    }

    default HighChartsSerieBean getHighChartsBean(String code, Double frequency, Double total) {
        HighChartsSerieBean bean = new HighChartsSerieBean();
        bean.setTotal(total);
        bean.setY(frequency);
        bean.setName(code);
        bean.setDrilldown(code);
        return bean;
    }

    boolean deleteObservationById(Integer id);

    boolean deleteObservationByMoment(Double moment);

    List<RegisterItem> getAllObservations();

    List<RegisterItem> getObservationById(UUID uuid);

    boolean saveObservation(Double videoTime, Category... categories);

    boolean saveObservation(RegisterItem item);

    boolean saveObservation(SceneWrapper sceneWrapper);

    RegisterItem loadCategoriesByCode(RegisterItem scene, List<Category> categories);

    HighChartsWrapper getObservationStats();

    default double getTotals(List<Pair<CategoryData, Double>> data) {
        double counter = 0;
        for (Pair<CategoryData, Double> item : data) {
            counter += item.getValue();
        }
        return counter;
    }

    List<Pair<CategoryData, Double>> getObservationRegisterVisibility(List<RegisterItem> register);

    HighChartsWrapper getRegisterStatsByCategory();

}
