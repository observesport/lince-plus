package com.lince.observer.math.service;

import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.LinceQualifier.DesktopQualifier;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.instrumentoObservacional.RootInstrumentoObservacional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * Container for legacy migration results
 */
record LegacyMigrationResult(
    Registro register,
    InstrumentoObservacional instrument
) {}

/**
 * lince-scientific-desktop
 * .app.service
 *
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 * <p>
 * Every public method that reads or replaces the legacy {@link Registro} / {@link InstrumentoObservacional}
 * singletons runs under {@link LegacyBridge#withLegacyModel}, the lock the LO-349 legacy importers and exporters
 * hold, so a legacy kappa or migration never swaps the singletons in the middle of a REST legacy export.
 */
@Service
public class LegacyConverterService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final CategoryService categoryService;
    private final AnalysisService analysisService;
    private final LegacyBridge legacyBridge;

    @Autowired
    public LegacyConverterService(@DesktopQualifier CategoryService categoryService,
                                  @DesktopQualifier AnalysisService analysisService,
                                  LegacyBridge legacyBridge) {
        this.categoryService = categoryService;
        this.analysisService = analysisService;
        this.legacyBridge = legacyBridge;
    }

    /**
     * Migrates both register and instrument data to legacy format
     * @param registerId UUID of the register to migrate
     * @return LegacyMigrationResult containing both migrated register and instrument
     */
    public LegacyMigrationResult migrateDataToLegacy(UUID registerId) {
        return legacyBridge.withLegacyModel(() -> {
            InstrumentoObservacional instrument = migrateDataToLegacyInstrument();
            Registro register = migrateDataToLegacyRegister(true, registerId);
            return new LegacyMigrationResult(register, instrument);
        });
    }

    /**
     * Legacy compatibility method that migrates only register data
     * @param registerId UUID of the register to migrate
     * @return migrated Registro
     */
    public Registro migrateRegisterToLegacy(UUID registerId) {
        return migrateDataToLegacy(registerId).register();
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
    public InstrumentoObservacional migrateDataToLegacyInstrument() {
        return legacyBridge.withLegacyModel(this::migrateDataToLegacyInstrumentLocked);
    }

    private InstrumentoObservacional migrateDataToLegacyInstrumentLocked() {
        InstrumentoObservacional.loadNewInstance();
        InstrumentoObservacional i = InstrumentoObservacional.getInstance();
        RootInstrumentoObservacional root = (RootInstrumentoObservacional) i.getModel().getRoot();
        DefaultMutableTreeNode variable = (DefaultMutableTreeNode) root.getChildAt(2);
        for (Criteria c : categoryService.getObservationTool()) {
            try {
                String criteriaName = c.getName();
                String criteriaDescription = c.getDescription();
                InstrumentoObservacional.getInstance().addHijo(variable, criteriaName);
                Criterio[] cs = i.getCriterios();
                Criterio criterio = cs[cs.length - 1];
                criterio.setDescripcion(criteriaDescription);
                criterio.setPersistente(c.isPersistence());
                //insert internal Categories
                addCategoriesToLegacyCriteria(criterio, c);
            } catch (Exception e) {
                log.error(getClass().getEnclosingMethod().toString(), e);
            }
        }
        return i;
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
            Criteria parent = categoryService.findToolEntryByIdentifier(c.getParent(), null, null).getKey();
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
    public Registro migrateDataToLegacyRegister(boolean doCriteriaInstrumentSearch, UUID uuid) {
        return legacyBridge.withLegacyModel(() -> migrateDataToLegacyRegisterLocked(doCriteriaInstrumentSearch, uuid));
    }

    private Registro migrateDataToLegacyRegisterLocked(boolean doCriteriaInstrumentSearch, UUID uuid) {
        try {
            Registro.loadNewInstance();
            Registro r = Registro.getInstance();
            //Reutilizamos para obtener datos por uuid. El instrumento DEBE ser el mismo siempre
            List<Category> errors = new ArrayList<>();
            List<RegisterItem> register = (uuid == null) ? analysisService.getAllObservations() : analysisService.getObservationById(uuid);
            for (RegisterItem row : register) {
                Integer timeID = row.getVideoTimeMillis();
                Map<Criterio, Categoria> registeredData = new HashMap<>();
                for (Category c : row.getRegister()) {
                    Pair<Criteria, Category> dataPair = categoryService.findToolEntryByIdentifier(c.getParent(), null, null);
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
                log.error("Registro inconsistente: {}", msg);
            }
            return r;
        } catch (Exception e) {
            log.error(getClass().getEnclosingMethod().toString(), e);
            return null;
        }
    }


}
