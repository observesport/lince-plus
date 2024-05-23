package com.lince.observer.math.service;

import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.CategoryInformation;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.highcharts.HighChartsSerie;
import com.lince.observer.data.bean.highcharts.HighChartsWrapper;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.AnalysisServiceBase;
import com.lince.observer.data.service.CategoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final AtomicInteger idGenerator;

    @Autowired
    public AnalysisServiceImpl(CategoryService categoryService, DataHubService dataHubService) {
        this.categoryService = categoryService;
        this.dataHubService = dataHubService;
        this.idGenerator = new AtomicInteger();
    }

    @Override
    public boolean deleteRegisterById(Integer id) {
        return deleteRegisterById(id, dataHubService.getCurrentDataRegister());
    }

    @Override
    public boolean deleteMomentInfo(Double moment) {
        return deleteMomentInfo(moment, dataHubService.getCurrentDataRegister());
    }

    @Override
    public List<RegisterItem> getDataRegister() {
        ensureDataRegisterConsistency();
        return dataHubService.getCurrentDataRegister();
    }

    @Override
    public List<RegisterItem> getDataRegisterById(UUID uuid) {
        try {
            ensureDataRegisterConsistency();
            return dataHubService.getRegisterById(uuid).getRegisterData();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Pair<CategoryData, Double>> getAllRegisterVisibility(List<RegisterItem> register) {
        return getAllRegisterVisibility(register, dataHubService.getCriteria());
    }

    @Override
    public HighChartsWrapper getRegisterStatsByCategory() {
        List<RegisterItem> register = getOrderedRegister();
        List<Criteria> tool = dataHubService.getCriteria();
        return getRegisterStatsByCategory(register, tool);
    }

    @Override
    public boolean pushRegister(Double videoTime, Category... categories) {
        return pushRegister(dataHubService.getCurrentDataRegister(), videoTime,categories);
    }

    @Override
    public boolean pushRegister(RegisterItem item) {
        return pushRegister(item, dataHubService.getCurrentDataRegister());
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
                Pair<Criteria, Category> elem = categoryService.findDataById(null, c.getCode(), null);
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

    protected Integer generateID() {
        //analizamos todos los ids y devolvemos el maximo
        try {
            for (RegisterItem value : dataHubService.getCurrentDataRegister()) {
                Integer currentGroupID = value.getId() == null ? -1 : value.getId();
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


    @Override
    public HighChartsWrapper getRegisterStatsByScene() {
        HighChartsWrapper rtn = new HighChartsWrapper();
        final Pair<Integer, String> EMPTY_SERIES_VALUE = new Pair<>(-1, "Sin definir");
        final String SCENE_LABEL = "Escena";
        try {
            List<RegisterItem> userSceneData = getOrderedRegister();
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
                    for (CategoryData aux : categoryService.getChildren(cats.getId())) {
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
                        Pair<Criteria, Category> relatedData = categoryService.findDataById(null, sceneCriteria.getCode(), null);
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


}
