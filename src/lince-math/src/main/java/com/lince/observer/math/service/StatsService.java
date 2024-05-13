package com.lince.observer.math.service;

import com.lince.observer.data.bean.agreement.AgreementResult;
import com.lince.observer.ai.agreement.LinceDkproAdapter;
import com.lince.observer.data.bean.KeyValue;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Created by Alberto Soto. 31/3/24
 */
public interface StatsService {
    Logger log = LoggerFactory.getLogger(StatsService.class);
    List<KeyValue<String, Double>> calculateLegacyKappa(UUID firstRegister, UUID secondRegister);

    List<LinceRegisterWrapper> getDataRegister();

    List<Criteria> getObservationTool();

    default LinceDkproAdapter getAdapter(UUID... uuids) {
        return new LinceDkproAdapter(getDataRegister(), getObservationTool(), uuids);
    }

    /**
     * - Only valid for 2 observers -
     *
     * @param firstRegister  valid uuid in register
     * @param secondRegister valid uuid in register
     * @return agreement results
     */
    default List<AgreementResult> calculateKappaPro(UUID firstRegister, UUID secondRegister) {
        try {
            LinceDkproAdapter adapter = getAdapter(firstRegister, secondRegister);
            return adapter.calculateKappaIndex();
        } catch (Exception e) {
            log.error("StatsService for Kappa index via DKpro", e);
            return null;
        }
    }

    /**
     * - Only valid for 2 observers -
     *
     * @param firstRegister  valid uuid in register
     * @param secondRegister valid uuid in register
     * @return json array with contingency data for each category
     */
    default List<AgreementResult> getContingencyMatrix(UUID firstRegister, UUID secondRegister) {
        try {
            LinceDkproAdapter adapter = getAdapter(firstRegister, secondRegister);
            return adapter.getContingencyMatrix();
        } catch (Exception e) {
            log.error("StatsService for Kappa index via DKpro", e);
            return null;
        }
    }

    /**
     * - Valid for n>1 observers -
     *
     * @param selectedRegisters list of selected observers
     * @return agreement results
     */
    default List<AgreementResult> calculateKrippendorf(UUID... selectedRegisters) {
        LinceDkproAdapter adapter = getAdapter(selectedRegisters);
        return adapter.calculateKrippendorfAlphaAgreement();
    }

    /**
     * - Valid for n>1 observers -
     *
     * @param selectedRegisters list of selected observers
     * @return agreement results
     */
    default List<AgreementResult> calculateAgreementPercentage(UUID... selectedRegisters) {
        LinceDkproAdapter adapter = getAdapter(selectedRegisters);
        return adapter.calculatePercentageAgreement();
    }
}
