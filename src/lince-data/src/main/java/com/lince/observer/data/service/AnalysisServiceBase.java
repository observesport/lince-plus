package com.lince.observer.data.service;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.CategoryInformation;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.highcharts.HighChartsSerie;
import com.lince.observer.data.bean.highcharts.HighChartsWrapper;
import com.lince.observer.data.bean.wrapper.SceneWrapper;
import com.lince.observer.data.util.TimeCalculations;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Created by Alberto Soto. 23/5/24
 */
public abstract class AnalysisServiceBase implements AnalysisService {

    private final CategoryService categoryService;
    private final ProfileService profileService;
    private final TimeCalculations timeCalculations;

    protected AnalysisServiceBase(CategoryService categoryService, ProfileService profileService) {
        this.categoryService = categoryService;
        this.profileService = profileService;
        this.timeCalculations = new TimeCalculations();
    }

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

    protected boolean pushRegister(List<RegisterItem> registerList, RegisterItem item) {
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
        return pushRegister(registerList, new RegisterItem(convertSysMoment(videoTime), categories));
    }

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
                CollectionUtils.addAll(globalCounter, getClusteredObservationsByCriteria(cri, register));
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
            List<Pair<CategoryData, Double>> globalCounter = getObservationRegisterVisibility(register);
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

    @Override
    public HighChartsWrapper getObservationStats() {
        HighChartsWrapper rtn = new HighChartsWrapper();
        final Pair<Integer, String> EMPTY_SERIES_VALUE = new Pair<>(-1, "Sin definir");
        final String SCENE_LABEL = "Escena";
        try {
            List<RegisterItem> userSceneData = getSortedObservations();
            //montamos eje X
            for (RegisterItem data : userSceneData) {
                rtn.getxSeriesLabels().add(String.format("%s (%s seg)", (StringUtils.isEmpty(data.getName()) ? SCENE_LABEL : data.getName()), data.getVideoTimeTxt()));
            }
            rtn.getySeriesLabels().add(EMPTY_SERIES_VALUE); //valor de alias
            //para todas los criterios del sistema montamos eje Y
            for (CategoryData cats : categoryService.getCollection()) {
                //es criterio
                Criteria parent = (Criteria) cats;
                if (!parent.isInformationNode()) {
                    HighChartsSerie serie = new HighChartsSerie();
                    serie.setName(cats.getName());
                    rtn.getSeries().add(serie);
                    //para todos sus categorias
                    for (CategoryData aux : categoryService.getCategoriesByParent(cats.getId())) {
                        Category crit = (Category) aux;
                        rtn.getySeriesLabels().add(new Pair<>(crit.getId(), crit.getCode())); //valor de alias
                    }
                }
            }
            //Analizamos todas las escenas y buscamos recursivamente si coincide cada elemento en alguna categoría
            int sceneIndex = 0;
            for (Iterator<RegisterItem> it = userSceneData.iterator(); it.hasNext(); sceneIndex++) {
                //para cada una de las escenas, tengo que buscar que para cada categoría haya un criterio.
                //si no, tengo que introducir un cero en la serie asociada
                RegisterItem sceneData = it.next();
                //tenemos las escenas por indice de ejecucion y sabemos si se ha encontrado o no el valor
                //para esta escena, analizamos todos lo seleccionado
                for (HighChartsSerie serie : rtn.getSeries()) {
                    boolean hasRelatedRegister = false;
                    for (Category sceneCriteria : sceneData.getRegister()) {
                        //para cada serie asociada, que es una categoria por nombre, miro si tengo o no un registro asociado
                        Pair<Criteria, Category> relatedData = categoryService.findToolEntryByIdentifier(null, sceneCriteria.getCode(), null);
                        //cada escena tiene que estar añadida por orden
                        //rtn.getSeries().get(sceneIndex); -> aqui no estan las escenas
                        if (relatedData != null) {
                            if (StringUtils.equals(serie.getName(), relatedData.getKey().getName())) {
                                //tenemos la serie a la que hace referencia esta escena y estos valores
                                hasRelatedRegister = true;
                                Double value = Double.valueOf(relatedData.getValue().getId());
                                serie.getData().add(value);
                            }
                        } else {
                            log.error("No se encuentra el registro asociado a esta escena! Incongruencia de datos");
                        }
                    }
                    if (!hasRelatedRegister) {
                        Double value = Double.valueOf(EMPTY_SERIES_VALUE.getKey());
                        serie.getData().add(value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error en graficas ", e);
        }
        return rtn;
    }

    @Override
    public RegisterItem loadCategoriesByCode(RegisterItem scene, List<Category> categories) {
        if (CollectionUtils.isNotEmpty(categories)) {
            List<Category> dataValues = new ArrayList<>();
            for (Category userCategory : categories) {
                boolean doAdd = true;
                Category fullCat = (Category) categoryService.findCategoryByCode(userCategory.getCode());
                if (CategoryInformation.class.isAssignableFrom(fullCat.getClass())) {
                    if (StringUtils.isEmpty(userCategory.getNodeInformation())) {
                        doAdd = false;
                    } else {
                        fullCat.setNodeInformation(userCategory.getNodeInformation());
                    }
                }
                if (doAdd) {
                    dataValues.add(fullCat);
                }
            }
            scene.setRegister(dataValues);
        }
        return scene;
    }

    protected Integer generateID() {
        AtomicInteger idGenerator = new AtomicInteger();
        try {
            for (RegisterItem value : getAllObservations()) {
                int currentGroupID = value.getId() == null ? -1 : value.getId();
                if (value.getId() == null) {
                    value.setId(generateID()); //corregimos posible error de ids al recorrer
                }
                if (currentGroupID > idGenerator.get()) {
                    idGenerator.set(currentGroupID);
                }
            }
        } catch (Exception e) {
            log.error("generateId", e);
        }
        return idGenerator.incrementAndGet();
    }

    public boolean saveObservation(SceneWrapper sceneWrapper) {
        try {
            if (sceneWrapper.getMoment() != null) {
                RegisterItem scene = new RegisterItem();
                if (sceneWrapper.getId() != null) {
                    scene.setId(sceneWrapper.getId());
                }
                scene.setVideoTime(sceneWrapper.getMoment());
                scene.setFrames(timeCalculations.convertMsToFrames(scene.getVideoTimeMillis(), profileService.getCurrentFPSValue()));
                scene.setName(sceneWrapper.getName());
                scene.setDescription(sceneWrapper.getDescription());
                scene = loadCategoriesByCode(scene, sceneWrapper.getCategories());
                scene.setSaveDate(new Date());
                return saveObservation(scene);
            }
        } catch (Exception e) {
            log.error("register:setMomentData - push scene", e);
        }
        return false;
    }
}
