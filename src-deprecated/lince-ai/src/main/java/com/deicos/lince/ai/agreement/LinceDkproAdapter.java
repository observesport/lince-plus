package com.deicos.lince.ai.agreement;

import com.deicos.lince.data.bean.RegisterItem;
import com.deicos.lince.data.bean.categories.Category;
import com.deicos.lince.data.bean.categories.Criteria;
import com.deicos.lince.data.bean.wrapper.LinceRegisterWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import org.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import org.dkpro.statistics.agreement.coding.PercentageAgreement;
import org.dkpro.statistics.agreement.distance.NominalDistanceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * com.deicos.lince.ai.agreement
 * Class LinceDkproAdapter
 * 12/04/2019
 * <p>
 * Concentrates wrapping all efforts inside Lince for analysis
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class LinceDkproAdapter {
    private static final Logger log = LoggerFactory.getLogger(LinceDkproAdapter.class);
    private List<LinceRegisterWrapper> registers;
    private List<Criteria> criterias;
    private List<CodingAnnotationStudy> studies = new ArrayList<>();
    private int numObservers;


    public LinceDkproAdapter(List<LinceRegisterWrapper> registers, List<Criteria> criterias) {
        this(registers, criterias, null);
    }

    public LinceDkproAdapter(List<LinceRegisterWrapper> registers, List<Criteria> criterias, UUID... selectedRegisters) {
        this.criterias = criterias;
        try {
            //filtramos los registros por uuid. si es nulo añadimos todos
            if (selectedRegisters != null && selectedRegisters.length > 0) {
                List<LinceRegisterWrapper> auxList = new ArrayList<>();
                for (UUID uuid : selectedRegisters) {
                    for (LinceRegisterWrapper register : registers) {
                        if (register.equals(uuid)) {
                            auxList.add(register);
                        }
                    }
                }
                //si no hemos encontrado los mismos, patada
                if (auxList.size() != selectedRegisters.length) {
                    throw new AgreementException();
                }
                //all valid. Let's play
                this.registers = auxList;
            } else {
                this.registers = registers;
            }
        } catch (Exception e) {
            this.registers = registers;
            log.error("selecting observers", e);
        }
        setNumObservers();
        setStudies();
    }

    public List<LinceRegisterWrapper> getRegisters() {
        return registers;
    }

    public List<Criteria> getCriterias() {
        return criterias;
    }

    /**
     * Static value depending on registers in the system
     *
     * @return observer ratio
     */
    public int getNumObservers() {
        return numObservers;
    }

    /**
     * Returns the studies created from Lince Info
     *
     * @return valid dkpro information
     */
    public List<CodingAnnotationStudy> getStudies() {
        return studies;
    }

    /**
     * counts registers and set data for analysis perspective
     */
    private void setNumObservers() {
        numObservers = registers.size();
    }

    /**
     * Creates an study for each defined criteria.
     * Will load data from registers and observers, which can be tricky
     */
    private void setStudies() {
        for (Criteria cri : criterias) {
            CodingAnnotationStudy study = new CodingAnnotationStudy(getNumObservers());
            try {
                final int numScenes = getRegisters().get(0).getRegisterData().size();
                for (int i = 0; i < numScenes; i++) {
                    List<String> values = getStringValues(cri, i);
                    study.addItem(values.toArray());
                }
            } catch (Exception e) {
                log.error("Creating study for criteria: " + cri.getName());
            }
            studies.add(study);
        }
    }

    /**
     * tengo que recorrer las escenas segun el primero de ellos y obtener
     * Accedemos a pelo, ojo
     *
     * @param cri
     * @param scenePosition
     * @return
     */
    private List<String> getStringValues(Criteria cri, int scenePosition) {
        List<String> rtn = new ArrayList<>();
        try {
            for (int i = 0; i < getNumObservers(); i++) {
                rtn.add(getCriteriaValueForScene(cri, scenePosition, i));
            }
        } catch (Exception e) {
            log.error("Generando acuerdo vacio. Causa:", e.getCause());
            rtn = new ArrayList<>();
            for (int i = 0; i < getNumObservers(); i++) {
                rtn.add(null);
            }
        }
        return rtn;
    }

    /**
     * gets all scene and observed info for an user by index
     *
     * @param observer observer index
     * @return scenes and selected info
     */
    private List<RegisterItem> getObserverDataByIndex(int observer) {
        try {
            LinceRegisterWrapper aux = getRegisters().get(observer);
            if (aux != null && !aux.getRegisterData().isEmpty()) {
                return aux.getRegisterData();
            }
        } catch (Exception e) {
            log.error("getting scene information for dkpro:", e);
        }
        return null;
    }

    /**
     * @param cri           expected criteria observation
     * @param scenePosition int
     * @param observer      desired observer by index
     * @return category in position or null by default
     */
    private String getCriteriaValueForScene(Criteria cri, int scenePosition, int observer) {
        try {
            List<RegisterItem> r = getObserverDataByIndex(observer);
            if (CollectionUtils.isNotEmpty(r) && scenePosition < r.size()) {
                RegisterItem item = r.get(scenePosition);
                for (Category cat : item.getRegister()) {
                    if (cat.getParent().equals(cri.getId())) {
                        return getCategoryLabel(cat);
                    }
                }
            }
        } catch (Exception e) {
            log.error("getCriteriaValueForScene", e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Unique method for returning code value for analisis
     * Here users should set their own renderer
     *
     * @param cat Category
     * @return String for labeling. Null if error
     */
    private String getCategoryLabel(Category cat) {
        try {
            return cat.getCode();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Internal method for retrieving all data in all studies
     *
     * @param type test type
     * @return results of test
     */
    private List<AgreementResult> calculate(AgreementResultType type) {
        List<AgreementResult> rtn = new ArrayList<>();
        for (int i = 0; i < studies.size(); i++) {
            CodingAnnotationStudy study = studies.get(i);
            Criteria cri = criterias.get(i);
            AgreementResult value = new AgreementResult();
            value.setType(type);
            value.setCriteria(cri);
            Double agreement = null;
            Double disagreement = null;
            Double expectedDisagreement = null;
            try {
                switch (type) {
                    case KappaIndex:
                        FleissKappaAgreement kappa = new FleissKappaAgreement(study);
                        agreement = kappa.calculateAgreement();
                        expectedDisagreement = kappa.calculateExpectedAgreement();
                        break;
                    case KrippendorfAlpha:
                        KrippendorffAlphaAgreement alpha = new KrippendorffAlphaAgreement(study, new NominalDistanceFunction());
                        agreement = alpha.calculateAgreement();
                        disagreement = alpha.calculateObservedDisagreement();
                        expectedDisagreement = alpha.calculateExpectedDisagreement();
                        break;
                    case ContingencyMatrix:
                        LinceContingencyMatrixPrinter printer = new LinceContingencyMatrixPrinter(study);
                        value.setContingencyMatrix(printer.getContingencyMatrixJson(true));
                        break;
                    default:
                        PercentageAgreement pa = new PercentageAgreement(study);
                        agreement = pa.calculateAgreement();
                        break;
                }
            } catch (Exception e) {
                log.error("Calculating agreement", e);
            }
            value.setAgreement(agreement);
            value.setExpectedDisagreement(expectedDisagreement);
            value.setObservedDisagreement(disagreement);
            rtn.add(value);
        }
        return rtn;
    }

    public List<AgreementResult> calculateKappaIndex() {
        return calculate(AgreementResultType.KappaIndex);
    }

    public List<AgreementResult> calculatePercentageAgreement() {
        return calculate(AgreementResultType.PercentageAgreement);
    }

    public List<AgreementResult> calculateKrippendorfAlphaAgreement() {
        return calculate(AgreementResultType.KrippendorfAlpha);
    }

    public List<AgreementResult> getContingencyMatrix() {
        return calculate(AgreementResultType.ContingencyMatrix);
    }

}
