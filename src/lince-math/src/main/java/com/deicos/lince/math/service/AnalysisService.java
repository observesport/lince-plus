package com.deicos.lince.math.service;

import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.Category;
import com.deicos.lince.data.bean.categories.CategoryData;
import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.math.AppParams;
import com.deicos.lince.math.highcharts.HighChartsSerie;
import com.deicos.lince.math.highcharts.HighChartsSerieBean;
import com.deicos.lince.math.highcharts.HighChartsWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * lince-scientific-desktop
 * com.deicos.lince.app.service
 *
 * @author berto (alberto.soto@gmail.com)in 22/06/2016.
 * Description:
 */
@Service
public class AnalysisService {
    protected final CategoryService categoryService;
    private final DataHubService dataHubService;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private static AtomicInteger idGenerator = new AtomicInteger();

    @Autowired
    public AnalysisService(CategoryService categoryService, DataHubService dataHubService) {
        this.categoryService = categoryService;
        this.dataHubService = dataHubService;
    }

    /**
     * Normaliza el momento de sistema a dos decimales
     *
     * @param moment
     * @return
     */
    private Double convertSysMoment(Double moment) {
        return Math.round(moment * 100.0) / 100.0;
    }

    /**
     * Devuelve la lista ordenada de elementos
     *
     * @return
     */
    public List<RegisterItem> getOrderedRegister() {
        List<RegisterItem> current = getDataRegister();
        Collections.sort(current);
        return current;
    }

    /**
     * Itera la colleccion y devuelve el estado de su eliminacion
     *
     * @param moment
     * @return
     */
    public boolean deleteMomentInfo(Double moment) {
        boolean isDeleted = false;
        try {

            if (moment != null) {
                for (RegisterItem reg : dataHubService.getCurrentDataRegister()) {
                    if (reg.getVideoTime().doubleValue() == moment.doubleValue()
                            || convertSysMoment(reg.getVideoTime()).doubleValue() == convertSysMoment(moment).doubleValue()) {
                        dataHubService.getCurrentDataRegister().remove(reg);
                        isDeleted = true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("deleteMoment", e);
        }
        return isDeleted;
    }

    public List<RegisterItem> getDataRegister() {
        ensureDataRegisterConsistency();
        return dataHubService.getCurrentDataRegister();
    }

    public List<RegisterItem> getDataRegisterById(UUID uuid) {
        try {
            ensureDataRegisterConsistency();
            return dataHubService.getRegisterById(uuid).getRegisterData();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * TODO Xavi review: Se debe eliminar registro no valido o sin referencia. Avisar al usuario. Queda corrupto
     */
    private void ensureDataRegisterConsistency() {
        for (RegisterItem reg : dataHubService.getCurrentDataRegister()) {
            int i = 0;
            for (Category c : reg.getRegister()) {
                reg.getRegister().set(i, categoryService.findDataById(null, c.getCode(), null).getValue());
                i++;
            }
        }
    }

    /**
     * TODO Xavi review: no tendriamos que hacer un set directo. Aqui tendríamos que buscar por nombre el criterio equivalente en toda la colección
     *
     *
     *
     * @param dataRegister
     */
    public void setDataRegister(List<RegisterItem> dataRegister) {
        dataHubService.getCurrentDataRegister().setAll(dataRegister);
    }

    /**
     * Searchs if time, id or saveTime exists in the register to return it
     *
     * @param searchValues
     * @return
     */
    private RegisterItem findRegister(RegisterItem searchValues) {
        for (RegisterItem reg : dataHubService.getCurrentDataRegister()) {
            try {
                if ((reg.getId() != null && reg.getId().equals(searchValues.getId()))
                        || NumberUtils.compare(reg.getVideoTime(), searchValues.getVideoTime()) == 0
                        || (reg.getSaveDate() != null && reg.getSaveDate().equals(searchValues.getSaveDate()))) {
                    return reg;
                }
            } catch (Exception e) {
                log.error("searching results");
            }

        }
        return null;
    }

    /**
     * Inserta un registro generado a partir de una colección de categorías
     *
     * @param videoTime
     * @param categories
     */
    public boolean pushRegister(Double videoTime, Category... categories) {
        return pushRegister(new RegisterItem(convertSysMoment(videoTime), categories));
    }

    /**
     * Inserta el registro introducido, buscando parámetros similares
     *
     * @param item
     */
    public boolean pushRegister(RegisterItem item) {
        boolean isNew = false;
        //LinceDataHelper dataHelper = LinceDataHelper.getInstance();
        RegisterItem sysRegister = findRegister(item);
        if (sysRegister == null) {
            item.setId(generateID());
            dataHubService.getCurrentDataRegister().add(item);
        } else {
            BeanUtils.copyProperties(item, sysRegister, AppParams.getNullPropertyNames(item));
            //dataHelper.addLogInfo("Modificados parametros observacionales en el momento " + sysRegister.getVideoTime().toString());
        }
        return true;
    }


    private Integer generateID() {
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


    /**
     * TODO: reduce. Pasar a functional. Siempre hacemos los mismos calculos
     *
     * @return
     */
    public HighChartsWrapper getRegisterStatsByScene() {
        HighChartsWrapper rtn = new HighChartsWrapper();
        final Pair<Integer, String> EMPTY_SERIES_VALUE = new Pair<>(-1, "Sin definir");
        final String SCENE_LABEL = "Escena";
        try {
            List<RegisterItem> userSceneData = getOrderedRegister();
            //montamos eje X
            for (RegisterItem data : userSceneData) {
                rtn.getxSeriesLabels().add(String.format("%s (%s seg)",
                        (StringUtils.isEmpty(data.getName()) ? SCENE_LABEL : data.getName()),
                        data.getVideoTime()));
            }
            rtn.getySeriesLabels().add(EMPTY_SERIES_VALUE); //valor de alias
            //para todas los criterios del sistema montamos eje Y
            for (CategoryData cats : categoryService.getCollection()) {
                //es criterio
                HighChartsSerie serie = new HighChartsSerie();
                serie.setName(cats.getName());
                rtn.getSeries().add(serie);
                //para todos sus categorias
                for (CategoryData aux : categoryService.getChildren(cats.getId())) {
                    Category crit = (Category) aux;
                    rtn.getySeriesLabels().add(new Pair<>(crit.getId(), crit.getCode())); //valor de alias
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
                                Double value = new Double(relatedData.getValue().getId());
                                //HighChartsSerieBean valBean = new HighChartsSerieBean();
                                //valBean.setY(value);
                                serie.getData().add(value);
                            }
                        } else {
                            log.error("No se encuentra el registro asociado a esta escena! Incongruencia de datos");
                        }
                    }
                    if (!hasRelatedRegister) {
                        Double value = new Double(EMPTY_SERIES_VALUE.getKey());
                        //HighChartsSerieBean valBean = new HighChartsSerieBean();
                        //valBean.setY(value);
                        serie.getData().add(value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error en graficas ", e);
        }
        return rtn;
    }

    /**
     * Checks criteria
     * TODO Xavi review: Tenemos que pensar como atacar aquí
     * @param a
     * @param b
     * @return
     */
    private boolean isSameCriteria(CategoryData a, CategoryData b) {
        if (a != null && b != null) {
            return (StringUtils.equals(a.getCode(), b.getCode())
                    || a.getId().equals(b.getId()));
        }
        return false;
    }


    private List<Pair<CategoryData, Double>> getRegisterVisibility(Criteria cri,
                                                                   List<RegisterItem> userSceneData) {
        List<Pair<CategoryData, Double>> rtnValues = new ArrayList<>();
        try {
            if (cri != null) {
                //Item visibility
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

    public double getTotals(List<Pair<CategoryData, Double>> data) {
        //Let's add totals
        double counter = 0;
        for (Pair<CategoryData, Double> item : data) {
            counter += item.getValue();
        }
        return counter;
    }


    private Double getFrecuency(Double item, Double total) {
        if (item != null && total != null)
            return (item * 100) / total;
        else
            return null;
    }

    public List<Pair<CategoryData, Double>> getAllRegisterVisibility(List<RegisterItem> register) {
        List<Pair<CategoryData, Double>> globalCounter = new ArrayList<>();
        try {
            for (Criteria cri : dataHubService.getCriteria()) {
                CollectionUtils.addAll(globalCounter, getRegisterVisibility(cri, register));
            }
        } catch (Exception e) {
            log.error("global register count", e);
        }
        return globalCounter;
    }


    private HighChartsSerieBean getBean(String code, Double frecuency, Double total) {
        HighChartsSerieBean bean = new HighChartsSerieBean();
        bean.setTotal(total);
        bean.setY(frecuency);
        bean.setName(code);
        bean.setDrilldown(code);
        return bean;
    }

    /**
     * @return Total aparience status wrapper to highcharts
     */
    public HighChartsWrapper getRegisterStatsByCategory() {
        HighChartsWrapper rtn = new HighChartsWrapper();
        HighChartsWrapper drillDown = new HighChartsWrapper();
        try {
            //obtenemos datos de contabilización
            List<RegisterItem> register = getOrderedRegister();
            List<Criteria> tool = dataHubService.getCriteria();
            List<Pair<CategoryData, Double>> globalCounter = getAllRegisterVisibility(register);
            Double total = getTotals(globalCounter);
            //tenemos las categorias sin agrupar, con sus totales
            //Montamos la lista de los padres
            HighChartsSerie rootSerie = new HighChartsSerie();
            for (Criteria cri : tool) {
                double totalPerCategory = 0;
                HighChartsSerie childSerie = new HighChartsSerie();
                for (Pair<CategoryData, Double> item : globalCounter) {
                    //si es el mismo padre
                    if (item.getKey().getParent().equals(cri.getId())) {
                        totalPerCategory += item.getValue();
                        childSerie.getDataBean().add(getBean(item.getKey().getName(),
                                getFrecuency(item.getValue(), total),
                                item.getValue()));
                    }
                }
                childSerie.setName(cri.getName());
                double percentPerCategory = getFrecuency(totalPerCategory, total);
                rootSerie.getDataBean().add(getBean(cri.getName(), percentPerCategory, totalPerCategory));
                drillDown.getSeries().add(childSerie);
            }
            rtn.getDrilldown().add(drillDown);
            rtn.getSeries().add(rootSerie);
        } catch (Exception e) {
            log.error("Error en graficas ", e);
        }
        return rtn;
    }
}