package com.deicos.lince.math.service;

import com.deicos.lince.ai.agreement.AgreementResult;
import com.deicos.lince.ai.agreement.LinceDkproAdapter;
import com.deicos.lince.data.LegacyStatsHelper;
import com.deicos.lince.data.bean.KeyValue;
import lince.modelo.InstrumentoObservacional.Criterio;
import lince.modelo.InstrumentoObservacional.InstrumentoObservacional;
import lince.modelo.Registro;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * com.deicos.lince.math.service
 * Class StatsService
 * 14/03/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Service
public class StatsService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DataHubService dataHubService;
    private final LegacyConverterService legacyConverterService;


    @Autowired
    public StatsService(DataHubService dataHubService, LegacyConverterService legacyConverterService) {
        this.dataHubService = dataHubService;
        this.legacyConverterService = legacyConverterService;
    }

    /**
     * Legacy method, using Lince 1 version
     *
     * @param firstRegister  valid uuid in register
     * @param secondRegister valid uuid in register
     * @return List of criteria + value
     */
    public List<KeyValue<String, Double>> calculateKappa(UUID firstRegister, UUID secondRegister) {
        try {
            List<KeyValue<String, Double>> response = new ArrayList<>();
            Registro old1 = legacyConverterService.migrateDataToLegacy(firstRegister);
            Registro old2 = legacyConverterService.migrateDataToLegacy(secondRegister);
            Criterio[] criteriaList = InstrumentoObservacional.getInstance().getCriterios();
            List<Object> items = new ArrayList<>();
            CollectionUtils.addAll(items, criteriaList);
            List<Double> kappas = LegacyStatsHelper.getKappaValues(old1, old2, items);
            for (int i = 0; i < kappas.size(); i++) {
                KeyValue<String, Double> data = new KeyValue<>();
                data.key = criteriaList[i].getNombre();
                data.value = kappas.get(i);
                response.add(data);
            }
            Double result = LegacyStatsHelper.calcularMedia(kappas);
            response.add(new KeyValue<>("Total", result));
            return response;
        } catch (Exception e) {
            log.error("StatsService for Kappa index", e);
            return null;
        }
    }


    private LinceDkproAdapter getAdapter(UUID... uuids) {
        return new LinceDkproAdapter(dataHubService.getDataRegister()
                , dataHubService.getCriteria()
                , uuids);
    }

    /**
     * - Only valid for 2 observers -
     *
     * @param firstRegister  valid uuid in register
     * @param secondRegister valid uuid in register
     * @return agreement results
     */
    public List<AgreementResult> calculateKappaPro(UUID firstRegister, UUID secondRegister) {
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
    public List<AgreementResult> getContingencyMatrix(UUID firstRegister, UUID secondRegister) {
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
    public List<AgreementResult> calculateKrippendorf(UUID... selectedRegisters) {
        LinceDkproAdapter adapter = getAdapter(selectedRegisters);
        return adapter.calculateKrippendorfAlphaAgreement();
    }

    /**
     * - Valid for n>1 observers -
     *
     * @param selectedRegisters list of selected observers
     * @return agreement results
     */
    public List<AgreementResult> calculateAgreementPercentage(UUID... selectedRegisters) {
        LinceDkproAdapter adapter = getAdapter(selectedRegisters);
        return adapter.calculatePercentageAgreement();
    }
}
