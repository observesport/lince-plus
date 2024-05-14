package com.lince.observer.data.service;

import com.lince.observer.data.bean.agreement.AgreementResult;
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

    /**
     * - Only valid for 2 observers -
     *
     * @param firstRegister  valid uuid in register
     * @param secondRegister valid uuid in register
     * @return agreement results
     */
    List<AgreementResult> calculateKappaPro(UUID firstRegister, UUID secondRegister);

    /**
     * - Only valid for 2 observers -
     *
     * @param firstRegister  valid uuid in register
     * @param secondRegister valid uuid in register
     * @return json array with contingency data for each category
     */
    List<AgreementResult> getContingencyMatrix(UUID firstRegister, UUID secondRegister);

    /**
     * - Valid for n>1 observers -
     *
     * @param selectedRegisters list of selected observers
     * @return agreement results
     */
    List<AgreementResult> calculateKrippendorf(UUID... selectedRegisters);

    /**
     * - Valid for n>1 observers -
     *
     * @param selectedRegisters list of selected observers
     * @return agreement results
     */
    List<AgreementResult> calculateAgreementPercentage(UUID... selectedRegisters);
}
