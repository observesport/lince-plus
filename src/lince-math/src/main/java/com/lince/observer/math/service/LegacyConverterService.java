package com.lince.observer.math.service;

import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.data.service.SessionService;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.legacy.FilaRegistro;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.instrumentoObservacional.RootInstrumentoObservacional;
import javafx.collections.FXCollections;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * lince-scientific-desktop
 * .app.service
 *
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 * <p>
 */
@Service
public class LegacyConverterService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final CategoryService categoryService;
    private final AnalysisService analysisService;
    private final DataHubService dataHubService;

    @Autowired
    public LegacyConverterService(@DesktopQualifier CategoryService categoryService,
                                  @DesktopQualifier AnalysisService analysisService,
                                  @DesktopQualifier DataHubService dataHubService) {
        this.categoryService = categoryService;
        this.analysisService = analysisService;
        this.dataHubService = dataHubService;
    }

    /**
     * Loads into the system loaded legacy data
     */
    public void migrateDataFromLegacy() {
        migrateDataFromLegacyInstrument();
        migrateDataFromLegacyRegister();
    }

    /**
     * Migrates current structure to Legacy data for export in legacy module
     */
    public Registro migrateDataToLegacy() {
        migrateDataToLegacyInstrument();
        return migrateDataToLegacyRegister(true, null);
    }

    public Registro migrateDataToLegacy(UUID registerId) {
        migrateDataToLegacyInstrument();
        return migrateDataToLegacyRegister(true, registerId);
    }

    /**
     * Creates a new user profile, sets as current and stores new UserData
     *
     * @param observerName observer name
     * @param lst          register
     */
    public void saveRegisterForNewUser(String observerName, List<RegisterItem> lst) {
        try {
            if (lst != null && !lst.isEmpty()) {
                UserProfile profile = new UserProfile();
                profile.setUserName(StringUtils.defaultIfEmpty(observerName, "New observer"));
                List<LinceRegisterWrapper> newItems = new ArrayList<>();
                LinceRegisterWrapper aux = new LinceRegisterWrapper();
                aux.setRegisterData(FXCollections.observableArrayList(lst));
                aux.setUserProfile(profile);
                newItems.add(aux);
                dataHubService.addDataRegister(newItems);
            }
        } catch (Exception e) {
            log.error("Saving imported data", e);
        }
    }

    /**
     * Cast old categoria into from Legacy new System Category
     *
     * @param c Legacy Category
     * @return new Category
     */
    private Category getCategoryFromLegacy(Criteria cri, Categoria c, Integer id) {
        try {
            Category newCat = new Category();
            newCat.setDescription(c.getDescripcion());
            newCat.setName(c.getNombre());
            newCat.setCode(c.getCodigo());
            newCat.setParent(cri.getId()); //it's very important setting the parent criteria to link it
            if (id != null) {
                newCat.setId(id);
            }
            return newCat;
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
            return null;
        }
    }

    /**
     * Returns a non-existing-generated code for the criteria
     *
     * @param c criteria
     * @param l currentlist
     * @return no existing code in the list
     */
    private String getValidCode(Criterio c, List<Criteria> l) {
        boolean isValid = true;
        try {
            int random = (int) (Math.random() * 100 + 1);
            String value = c.getNombre().replaceAll("[^a-zA-Z0-9]", "");
            value += random;
            //let's check if exists
            for (Criteria cri : l) {
                if (StringUtils.equals(value, cri.getCode())) {
                    isValid = false;
                }
            }
            //recursividad al canto. Ojo condicion de salida
            if (!isValid) {
                return getValidCode(c, l);
            }
            return value;
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Cast old Criteria and inner categories from Legacy into new System Criteria
     *
     * @param c legacy Criteria
     * @return Criteria
     */
    private Criteria getCriteriaFromLegacy(Criterio c, Integer id, List<Criteria> l) {
        try {
            if (!c.empty() && c.getCategoriasHijo().length > 0) {
                Criteria crit = new Criteria();
                crit.setName(c.getNombre());
                if (id != null) {
                    crit.setId(id);
                }
                crit.setCode(getValidCode(c, l));
                //crit.setCode(UUID.fromString(crit.getName()).toString());
                crit.setDescription(c.getDescripcion());
                LinkedList<Category> innerCategories = new LinkedList<>();
                int catId = 1;//very important! if not, children id will be parent id when 0
                for (Categoria cat : c.getCategoriasHijo()) {
                    innerCategories.add(getCategoryFromLegacy(crit, cat, id * 10 + catId));
                    catId++;
                }
                crit.setInnerCategories(innerCategories);
                return crit;
            }
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
        }
        return null;//new Criteria();
    }

    /**
     * Clears the internal categoryService filling it with the contents of the instrumento observacional
     * from legacy structure
     */
    private void migrateDataFromLegacyInstrument() {
        try {
            InstrumentoObservacional i = InstrumentoObservacional.getInstance();
            if (i != null) {
                Criterio[] criterios = i.getCriterios();
                if (criterios != null && criterios.length > 0) {
                    List<Criteria> l = new ArrayList<>();
                    int id = 1;//si fuera 0 en js se evalua a false y no se puede abrir (flipa)
                    for (Criterio c : i.getCriterios()) {
                        Criteria aux = getCriteriaFromLegacy(c, id, l);
                        if (aux != null) {
                            l.add(aux);
                            id++;
                        }
                    }
                    if (l.size() > 0) {
                        categoryService.clearAll();
                        categoryService.pushAll(l);
                    }
                }
            }
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
        }

    }

    /**
     * Uses previous loaded tool and loads all registered data from legacy into the new structure
     */
    private void migrateDataFromLegacyRegister() {
        try {
            Registro r = Registro.getInstance();
            int id = 1;
            List<RegisterItem> lst = new ArrayList<>();
            for (FilaRegistro entry : r.datosVariables) {
                RegisterItem register = new RegisterItem();
                register.setId(id);
                register.setSaveDate(new Date());
                register.setVideoTime((double) (entry.getMilis() / 1000));
                register.setDescription(StringUtils.EMPTY);
                register.setName("f." + entry.getFrames());
                register.setFrames(entry.getFrames());
                List<Category> categories = new ArrayList<>();
                for (Map.Entry<Criterio, Categoria> data : entry.getRegistro().entrySet()) {
                    Categoria cat = data.getValue();
                    //Category pairCat = (Category) categoryService.findCategoryByCode(cat.getCodigo());
                    Pair<Criteria, Category> pairCat = categoryService.findDataById(null, cat.getCodigo(), null);
                    //Criteria cri = (Criteria)categoryService.findCategoryByCode(data.getKey());
                    if (pairCat != null && pairCat.getValue() != null) {
                        categories.add(pairCat.getValue());
                    } else {
                        log.error("criterio no valido" + pairCat.getValue().toString());
                        Criteria aux = Criteria.getRecoveryCriteria();
                        CategoryData tempData = categoryService.findCategoryByCode(aux.getCode());
                        if (tempData == null) {
                            categoryService.getCriteria().add(aux);
                        }
                    }

                }
                if (categories.size() > 0) {
                    register.setRegister(categories);
                }
                lst.add(register);
                id++;
            }
            if (lst.size() > 0) {
                saveRegisterForNewUser("Lince v1 observer", lst);
            }
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
        }

    }

    /**
     * Maximizes exception control for export actions
     *
     * @param legacyCriteria base legacy data
     * @param linceCriteria  new component
     */
    private void addCategoriesToLegacyCriteria(Criterio legacyCriteria, Criteria linceCriteria) {
        if (linceCriteria != null) {
            List<Category> currentCategories = linceCriteria.getInnerCategories();
            boolean containsInformationNode = linceCriteria.isInformationNode();
            if (currentCategories != null && !currentCategories.isEmpty()) {
                for (Category cat : currentCategories) {
                    try {
                        InstrumentoObservacional.getInstance().addHijo(legacyCriteria, cat.getName());
                        Categoria categoria = (Categoria) legacyCriteria.getChildAt(legacyCriteria.getChildCount() - 1);
                        String code = cat.getCode();
                        if (containsInformationNode) {
                            code += cat.getId();
                        }
                        categoria.setCodigo(code);
                        categoria.setDescripcion(cat.getDescription());
                        //categoria.setNombre(cat.getName());
                        categoria.setParent(legacyCriteria);
                    } catch (Exception e) {
                        log.error(getClass().getEnclosingMethod().toString(), e);
                    }
                }
            }
        }
    }

    /**
     * Valid migration to Legacy instrument.
     * Adding items is done by reference, which justifies using an unique method for it.
     * Based on HoisanTool::import
     */
    private void migrateDataToLegacyInstrument() {

        InstrumentoObservacional.loadNewInstance();
        InstrumentoObservacional i = InstrumentoObservacional.getInstance();
        RootInstrumentoObservacional root = (RootInstrumentoObservacional) i.getModel().getRoot();
        DefaultMutableTreeNode variable = (DefaultMutableTreeNode) root.getChildAt(2);
        for (CategoryData c : categoryService.getCollection()) {
            try {
                Criteria criteria = (Criteria) c;
                String criteriaName = criteria.getName();
                String criteriaDescription = criteria.getDescription();
                InstrumentoObservacional.getInstance().addHijo(variable, criteriaName);
                Criterio[] cs = i.getCriterios();
                Criterio criterio = cs[cs.length - 1];
                criterio.setDescripcion(criteriaDescription);
                criterio.setPersistente(criteria.isPersistence());
                //insert internal Categories
                addCategoriesToLegacyCriteria(criterio, criteria);
            } catch (Exception e) {
                log.error(getClass().getEnclosingMethod().toString(), e);
            }
        }
    }


    private Criterio getLegacyCriteria(Criteria c) {
        try {
            Criterio rtn = new Criterio();
            rtn.setDescripcion(c.getDescription());
            rtn.setNombre(c.getName());
            rtn.setPersistente(c.isPersistence());
            return rtn;
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
        }
        return null;
    }

    private Categoria getLegacyCategory(Category c) {
        try {
            Categoria cat = new Categoria();
            cat.setNombre(c.getName());
            cat.setDescripcion(c.getDescription());
            String code = c.getCode();
            //Criteria parent = (Criteria) categoryService.findCategoryById(c.getParent());
            Criteria parent = categoryService.findDataById(c.getParent(), null, null).getKey();
            cat.setParent(getLegacyCriteria(parent));
            if (parent.isInformationNode()) {
                code += LinceDataConstants.CATEGORY_INFO_SUFIX;
                cat.setDescripcion(c.getNodeInformation());
            }
            cat.setCodigo(code);
            return cat;
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
        }
        return null;
    }

    /***
     * Avoid uuid problems searching data identities in previous loaded instrument
     * @param cri newSystem criteria
     * @param c newSystem category
     * @return legacy Pair Data value
     */
    private Pair<Criterio, Categoria> findInstrumentDataByCategory(Criteria cri, Category c) {
        try {
            InstrumentoObservacional i = InstrumentoObservacional.getInstance();
            for (Criterio rCrit : i.getCriterios()) {
                if (StringUtils.equals(rCrit.getNombre(), cri.getName())) {
                    for (Categoria rCat : rCrit.getCategoriasHijo()) {
                        if (StringUtils.equals(rCat.getCodigo(), c.getCode())) {
                            return new Pair<>(rCrit, rCat);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Not found instrument data", e);
        }
        return null;
    }

    /**
     * Valid migration to Legacy register.
     * Adding items is done by reference, which justifies using an unique method for it.
     * Based on HoisanTool::generateRegistros
     *
     * @param doCriteriaInstrumentSearch bool
     */
    private Registro migrateDataToLegacyRegister(boolean doCriteriaInstrumentSearch, UUID uuid) {
        try {
            Registro.loadNewInstance();
            Registro r = Registro.getInstance();
            //Reutilizamos para obtener datos por uuid. El instrumento DEBE ser el mismo siempre
            List<Category> errors = new ArrayList<>();
            List<RegisterItem> register = (uuid == null) ? analysisService.getDataRegister() : analysisService.getDataRegisterById(uuid);
            for (RegisterItem row : register) {
                Integer timeID = row.getVideoTimeMilis();
                Map<Criterio, Categoria> registeredData = new HashMap<>();
                for (Category c : row.getRegister()) {
                    Pair<Criteria, Category> dataPair = categoryService.findDataById(c.getParent(), null, null);
                    if (dataPair != null && dataPair.getKey() != null) {
                        Criteria parent = dataPair.getKey();
                        //si es nulo, es criterio no es correcto
                        Pair<Criterio, Categoria> rtn = null;
                        if (doCriteriaInstrumentSearch) {
                            //tiene UUID corregidos ??
                            rtn = findInstrumentDataByCategory(parent, c);
                        }
                        if (rtn == null || (rtn.getValue() == null && rtn.getKey() == null)) {
                            Categoria cat = getLegacyCategory(c);
                            Criterio cri = getLegacyCriteria(parent);
                            //tiene UUID corregidos
                            registeredData.put(cri, cat);
                        } else {
                            registeredData.put(rtn.getKey(), rtn.getValue());
                        }
                    } else {
                        errors.add(c);
                    }
                }
                r.addRow(timeID, registeredData, new HashMap<>());
            }
            if (!errors.isEmpty()) {
                StringBuilder categories = new StringBuilder();
                for (Category cat : errors) {
                    categories.append("-").append(cat.getCode()).append("\n");
                }
                String msg = String.format("Su registro tiene inconsistencias, normalmente causadas \n" +
                        "por modificaciones del instrumento, eliminando parámetros que se han utilizado. \n Por favor, " +
                        "corrije las observaciones de las siguientes categorías: \n %s", categories);
                JavaFXLogHelper.showMessageDialog("Registro inconsistente", msg);
                JavaFXLogHelper.addLogError(msg, new NullPointerException());
            }
            return r;
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
            return null;
        }
    }


}
