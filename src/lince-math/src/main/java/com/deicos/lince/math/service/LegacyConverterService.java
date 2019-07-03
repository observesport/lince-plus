package com.deicos.lince.math.service;

import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.Category;
import com.deicos.lince.data.bean.categories.CategoryData;
import com.deicos.lince.data.bean.categories.Criteria;
import lince.modelo.FilaRegistro;
import lince.modelo.InstrumentoObservacional.Categoria;
import lince.modelo.InstrumentoObservacional.Criterio;
import lince.modelo.InstrumentoObservacional.InstrumentoObservacional;
import lince.modelo.InstrumentoObservacional.RootInstrumentoObservacional;
import lince.modelo.Registro;
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
 * com.deicos.lince.app.service
 *
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 * <p>
 * todo asf summer'17 check where uuid problem is
 */
@Service
public class LegacyConverterService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final CategoryService categoryService;
    private final AnalysisService analysisService;

    @Autowired
    public LegacyConverterService(CategoryService categoryService, AnalysisService analysisService) {
        this.categoryService = categoryService;
        this.analysisService = analysisService;
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
     * TODO XAVI review: re-doublecheck
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
            String value = StringUtils.trim(StringUtils.substring(c.getNombre(), 0, 3)) + random;
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
                int catId = 0;
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
        return null;
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
                    int id = 0;
                    for (Criterio c : i.getCriterios()) {
                        l.add(getCriteriaFromLegacy(c, id, l));
                        id++;
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
            //TODO asf (sum'17) si no esta cargado el instrumento lo deberíamos recuperamos del interno en registro
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
                    categories.add(pairCat.getValue());
                }
                if (categories.size() > 0) {
                    register.setRegister(categories);
                }
                lst.add(register);
                id++;
            }
            if (lst.size() > 0) {
                analysisService.setDataRegister(lst);
            }
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
        }

    }

    /**
     * Valid migration to Legacy instrument.
     * Adding items is done by reference, which justifies using an unique method for it.
     * Based on HoisanTool::import
     */
    private void migrateDataToLegacyInstrument() {
        try {
            InstrumentoObservacional.loadNewInstance();
            InstrumentoObservacional i = InstrumentoObservacional.getInstance();
            RootInstrumentoObservacional root = (RootInstrumentoObservacional) i.getModel().getRoot();
            DefaultMutableTreeNode variable = (DefaultMutableTreeNode) root.getChildAt(2);
            for (CategoryData c : categoryService.getCollection()) {
                Criteria criteria = (Criteria) c;
                String criteriaName = c.getName();
                String criteriaDescription = c.getDescription();
                InstrumentoObservacional.getInstance().addHijo(variable, criteriaName);
                Criterio[] cs = i.getCriterios();
                Criterio criterio = cs[cs.length - 1];
                criterio.setDescripcion(criteriaDescription);
                //TODO asf summer'17: importante set de persistencia(bool) (no está preparado aun?)
                //criterio.setPersistente();
                //insert internal Categories
                for (Category cat : criteria.getInnerCategories()) {
                    InstrumentoObservacional.getInstance().addHijo(criterio, cat.getName());
                    Categoria categoria = (Categoria) criterio.getChildAt(criterio.getChildCount() - 1);
                    categoria.setCodigo(cat.getCode());
                    categoria.setDescripcion(cat.getDescription());
                    //categoria.setNombre(cat.getName());
                    categoria.setParent(criterio);
                }
            }
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
        }
    }


    private Criterio getLegacyCriteria(Criteria c) {
        try {
            Criterio rtn = new Criterio();
            rtn.setDescripcion(c.getDescription());
            rtn.setNombre(c.getName());
            //rtn.setPersistente(); TODO asf summer'17 verify
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
            cat.setCodigo(c.getCode());
            //Criteria parent = (Criteria) categoryService.findCategoryById(c.getParent());
            Criteria parent = categoryService.findDataById(c.getParent(), null, null).getKey();
            cat.setParent(getLegacyCriteria(parent));
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
            for (RegisterItem row : (uuid == null) ? analysisService.getDataRegister() : analysisService.getDataRegisterById(uuid)) {
                Integer timeID = row.getVideoTimeMilis();
                Map<Criterio, Categoria> registeredData = new HashMap<>();
                for (Category c : row.getRegister()) {
                    Criteria parent = categoryService.findDataById(c.getParent(), null, null).getKey();
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
                }
                r.addRow(timeID, registeredData, new HashMap<>());
            }
            return r;
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
            return null;
        }
    }


}
