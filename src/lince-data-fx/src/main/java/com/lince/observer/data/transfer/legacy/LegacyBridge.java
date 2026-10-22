package com.lince.observer.data.transfer.legacy;

import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.CategoryData;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.legacy.FilaRegistro;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Headless replacement for {@code RootLayoutController.ensureExportCompatibility()} /
 * {@code ensureImportCompatibility()} (LO-349): converts an {@code ILinceProject} into the legacy
 * {@link InstrumentoObservacional} / {@link Registro} state that the Lince 1, Theme 5, SAS, SDIS-GSEQ and HOISAN
 * code reads, and converts legacy instruments and rows back into modern {@link Criteria} and
 * {@link RegisterItem}s. It keeps a {@link CriteriaMapping} so the criteria ids selected through the API map to
 * the right {@link Criterio} objects.
 * <p>
 * The conversion rules are ported from {@code LegacyConverterService} (lince-math), but driven by the project
 * passed in instead of the live {@code CategoryService} / {@code AnalysisService}, so the same code serves the
 * desktop REST service, the JavaFX menu and the cloud edition. It has no UI: no JavaFX, Swing, AWT or dialogs.
 * <p>
 * <b>Singletons and concurrency.</b> The legacy model is global: {@code InstrumentoObservacional.getInstance()}
 * and {@code Registro.getInstance()} are static singletons, and legacy nodes call back into them (for example
 * {@code Criterio.setDescripcion} notifies {@code InstrumentoObservacional.getInstance()}). A conversion therefore
 * replaces the singletons, and anything that reads them afterwards (the {@code exportTo*} methods,
 * {@code Registro.getName()}, file loading that checks against the current instrument) must run before another
 * conversion replaces them again. Two conversions must never run concurrently. Every public method holds one
 * class-wide lock, and callers that convert and then read the legacy state must do both inside
 * {@link #withLegacyModel(Supplier)} or {@link #export(ExportSelection, Function)}, which hold that lock for the
 * whole unit of work. The lock is reentrant, so nested calls are safe. The JavaFX legacy panels share the same
 * singletons; a REST export while one of them is open replaces what it shows, exactly as the menu export does
 * today.
 * <p>
 * <b>Other callers.</b> {@code LegacyConverterService} (every public method), {@code StatsServiceImpl.calculateLegacyKappa}
 * and {@code RootLayoutController.handleNew} (through {@code CurrentProjectProvider.resetLegacyRegister}) hold this
 * lock too, and any new code that touches {@link Registro} or {@link InstrumentoObservacional} must do the same.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class LegacyBridge {

    private static final Logger log = LoggerFactory.getLogger(LegacyBridge.class);
    private static final Object LOCK = new Object();

    /**
     * Runs {@code work} while holding the legacy model lock.
     *
     * @param work conversion plus whatever reads the legacy singletons
     * @return the work result
     */
    public <T> T withLegacyModel(Supplier<T> work) {
        synchronized (LOCK) {
            return work.get();
        }
    }

    /**
     * Converts the selection with {@link #toLegacy(ExportSelection)} and runs {@code work} on it under the lock.
     *
     * @param selection resolved project, register and criteria
     * @param work      typically a legacy {@code exportTo*} call on the selected criteria
     * @return the work result
     */
    public <T> T export(ExportSelection selection, Function<LegacyProject, T> work) {
        return withLegacyModel(() -> work.apply(toLegacy(selection)));
    }

    /**
     * Replaces the legacy singletons with the selection's instrument and register (the headless
     * {@code ensureExportCompatibility()}). Rows whose categories cannot be resolved are kept without them and
     * reported in {@link LegacyProject#warnings()}.
     *
     * @param selection resolved project, register and criteria
     * @return the legacy state; valid while the caller holds the lock
     */
    public LegacyProject toLegacy(ExportSelection selection) {
        Objects.requireNonNull(selection, "selection");
        synchronized (LOCK) {
            List<Criteria> tool = nullSafe(selection.getProject().getObservationTool());
            CriteriaMapping mapping = buildInstrument(tool);
            InstrumentoObservacional instrument = InstrumentoObservacional.getInstance();
            Registro.loadNewInstance();
            Registro register = Registro.getInstance();
            List<String> warnings = new ArrayList<>();
            for (RegisterItem row : selection.getRows()) {
                addLegacyRow(register, instrument, tool, row, warnings);
            }
            return new LegacyProject(instrument, register, mapping, mapping.toLegacy(selection.getCriteria()), warnings);
        }
    }

    /**
     * Replaces the legacy instrument with {@code tool} and starts an empty legacy register. Legacy register
     * files are validated against the current instrument when loaded, so register importers call this first.
     *
     * @param tool criteria of the target project
     * @return the legacy state with an empty register and every criterion selected
     */
    public LegacyProject installInstrument(List<Criteria> tool) {
        synchronized (LOCK) {
            List<Criteria> criteria = nullSafe(tool);
            CriteriaMapping mapping = buildInstrument(criteria);
            Registro.loadNewInstance();
            return new LegacyProject(InstrumentoObservacional.getInstance(), Registro.getInstance(), mapping,
                    mapping.toLegacy(criteria), List.of());
        }
    }

    /**
     * Converts a legacy instrument into modern criteria (the instrument half of
     * {@code ensureImportCompatibility()}). Criteria without categories are skipped, as before. Criteria ids
     * start at 1 (0 is falsy in the dashboard) and category ids are {@code criteriaId * 10 + position}, as before.
     * Criteria codes are the name without non alphanumeric characters plus the criteria id, made unique; the
     * previous code used a random suffix.
     *
     * @param instrument legacy instrument, usually the singleton just loaded
     * @return criteria and the mapping to the legacy ones
     */
    public LegacyImport fromLegacyInstrument(InstrumentoObservacional instrument) {
        Objects.requireNonNull(instrument, "instrument");
        synchronized (LOCK) {
            List<Criteria> result = new ArrayList<>();
            CriteriaMapping mapping = new CriteriaMapping();
            int id = 1;
            for (Criterio criterio : instrument.getCriterios()) {
                if (criterio == null || criterio.empty() || criterio.getCategoriasHijo().length == 0) {
                    continue;
                }
                Criteria criteria = new Criteria();
                criteria.setId(id);
                criteria.setName(criterio.getNombre());
                criteria.setCode(uniqueCode(criterio.getNombre(), id, result));
                criteria.setDescription(criterio.getDescripcion());
                LinkedList<Category> categories = new LinkedList<>();
                int position = 1;
                for (Categoria categoria : criterio.getCategoriasHijo()) {
                    Category category = new Category();
                    category.setId(id * 10 + position);
                    category.setName(categoria.getNombre());
                    category.setCode(categoria.getCodigo());
                    category.setDescription(categoria.getDescripcion());
                    category.setParent(id);
                    categories.add(category);
                    position++;
                }
                criteria.setInnerCategories(categories);
                result.add(criteria);
                mapping.put(criteria, criterio);
                id++;
            }
            return new LegacyImport(result, mapping);
        }
    }

    /**
     * Converts legacy register rows into modern observations (the register half of
     * {@code ensureImportCompatibility()}). Categories are resolved by code against {@code tool}; an unresolved
     * category is dropped from its row and reported in {@code warnings} (the previous code attached it to a
     * synthetic "recovery" criteria).
     *
     * @param rows     legacy rows, for example {@code Registro.getInstance().datosVariables}
     * @param tool     criteria of the target project
     * @param warnings receives one message per unresolved category code
     * @return observations with ids from 1, in row order
     */
    public List<RegisterItem> fromLegacyRows(List<FilaRegistro> rows, List<Criteria> tool, List<String> warnings) {
        synchronized (LOCK) {
            List<Criteria> criteria = nullSafe(tool);
            List<RegisterItem> result = new ArrayList<>();
            int id = 1;
            for (FilaRegistro row : nullSafe(rows)) {
                RegisterItem item = new RegisterItem(row.getMilis() / 1000.0);
                item.setId(id++);
                item.setSaveDate(new Date());
                item.setDescription(StringUtils.EMPTY);
                item.setName("f." + row.getRegisterFrameValue());
                item.setFrames(row.getRegisterFrameValue());
                List<Category> categories = new ArrayList<>();
                for (Categoria categoria : row.getRegistro().values()) {
                    if (categoria == null) {
                        continue;
                    }
                    Category category = findCategoryByCode(criteria, categoria.getCodigo());
                    if (category != null) {
                        categories.add(category);
                    } else {
                        addOnce(warnings, "Category not found for code: " + categoria.getCodigo());
                    }
                }
                item.setRegister(categories);
                result.add(item);
            }
            return result;
        }
    }

    private CriteriaMapping buildInstrument(List<Criteria> tool) {
        InstrumentoObservacional.loadNewInstance();
        InstrumentoObservacional instrument = InstrumentoObservacional.getInstance();
        CriteriaMapping mapping = new CriteriaMapping();
        for (Criteria criteria : tool) {
            Criterio criterio = instrument.addCriterio(StringUtils.defaultString(criteria.getName()));
            criterio.setDescripcion(criteria.getDescription());
            criterio.setPersistente(criteria.isPersistence());
            for (Category category : nullSafe(criteria.getInnerCategories())) {
                Categoria categoria = instrument.addCategoria(criterio, StringUtils.defaultString(category.getName()));
                String code = category.getCode();
                if (criteria.isInformationNode()) {
                    code += category.getId();
                }
                categoria.setCodigo(code);
                categoria.setDescripcion(category.getDescription());
            }
            mapping.put(criteria, criterio);
        }
        return mapping;
    }

    private void addLegacyRow(Registro register, InstrumentoObservacional instrument, List<Criteria> tool,
                              RegisterItem row, List<String> warnings) {
        Map<Criterio, Categoria> registered = new HashMap<>();
        for (Category category : nullSafe(row.getRegister())) {
            Criteria parent = findCriteriaByEntryId(tool, category.getParent());
            if (parent == null) {
                addOnce(warnings, "Inconsistent register, criteria not found for category: " + category.getCode());
                continue;
            }
            Criterio criterio = findLegacyCriterio(instrument, parent);
            Categoria categoria = criterio == null ? null : findLegacyCategoria(criterio, category.getCode());
            if (categoria == null) {
                criterio = detachedCriterio(parent);
                categoria = detachedCategoria(category, parent, criterio);
            }
            registered.put(criterio, categoria);
        }
        Integer millis = row.getVideoTimeMillis();
        register.addRow(millis == null ? 0 : millis, registered, new HashMap<>());
    }

    /**
     * Same walk as {@code CategoryServiceOld.findTreeEntryById(id, null, null)}: the criteria whose own id, or one
     * of whose categories' id, equals {@code id}.
     */
    private static Criteria findCriteriaByEntryId(List<Criteria> tool, Integer id) {
        if (id == null) {
            return null;
        }
        for (Criteria criteria : tool) {
            if (id.equals(criteria.getId())) {
                return criteria;
            }
            for (Category category : nullSafe(criteria.getInnerCategories())) {
                if (id.equals(category.getId())) {
                    return criteria;
                }
            }
        }
        return null;
    }

    /**
     * Same walk as {@code CategoryServiceOld.findTreeEntryById(null, code, null)}; a code that first matches a
     * criteria resolves to no category, as before.
     */
    private static Category findCategoryByCode(List<Criteria> tool, String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        String realCode = CategoryData.isInformationNode(code) ? CategoryData.getInformationNodeCode(code) : code;
        for (Criteria criteria : tool) {
            if (StringUtils.equals(realCode, criteria.getCode())) {
                return null;
            }
            for (Category category : nullSafe(criteria.getInnerCategories())) {
                if (StringUtils.equals(realCode, category.getCode())) {
                    return category;
                }
            }
        }
        return null;
    }

    private static Criterio findLegacyCriterio(InstrumentoObservacional instrument, Criteria parent) {
        for (Criterio criterio : instrument.getCriterios()) {
            if (StringUtils.equals(criterio.getNombre(), parent.getName())) {
                return criterio;
            }
        }
        return null;
    }

    private static Categoria findLegacyCategoria(Criterio criterio, String code) {
        for (Categoria categoria : criterio.getCategoriasHijo()) {
            if (StringUtils.equals(categoria.getCodigo(), code)) {
                return categoria;
            }
        }
        return null;
    }

    private static Criterio detachedCriterio(Criteria criteria) {
        Criterio criterio = new Criterio();
        criterio.setDescripcion(criteria.getDescription());
        criterio.setNombre(criteria.getName());
        criterio.setPersistente(criteria.isPersistence());
        return criterio;
    }

    private static Categoria detachedCategoria(Category category, Criteria parent, Criterio legacyParent) {
        Categoria categoria = new Categoria();
        categoria.setNombre(category.getName());
        categoria.setDescripcion(category.getDescription());
        categoria.setParent(legacyParent);
        String code = category.getCode();
        if (parent.isInformationNode()) {
            code += LinceDataConstants.CATEGORY_INFO_SUFIX;
            categoria.setDescripcion(category.getNodeInformation());
        }
        categoria.setCodigo(code);
        return categoria;
    }

    private static String uniqueCode(String name, int id, List<Criteria> existing) {
        String base = StringUtils.defaultString(name).replaceAll("[^a-zA-Z0-9]", "");
        if (base.isEmpty()) {
            base = "C";
        }
        String candidate = base + id;
        int suffix = 1;
        while (codeExists(candidate, existing)) {
            candidate = base + id + "_" + suffix++;
        }
        return candidate;
    }

    private static boolean codeExists(String code, List<Criteria> existing) {
        return existing.stream().anyMatch(c -> StringUtils.equals(code, c.getCode()));
    }

    private static void addOnce(List<String> warnings, String message) {
        if (warnings != null && !warnings.contains(message)) {
            log.warn(message);
            warnings.add(message);
        }
    }

    private static <E> List<E> nullSafe(List<E> list) {
        return list == null ? List.of() : list;
    }
}
