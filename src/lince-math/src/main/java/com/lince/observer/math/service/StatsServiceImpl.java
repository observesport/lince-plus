package com.lince.observer.math.service;

import com.lince.observer.ai.agreement.LinceDkproAdapter;
import com.lince.observer.data.LegacyStatsHelper;
import com.lince.observer.data.bean.KeyValue;
import com.lince.observer.data.bean.agreement.AgreementResult;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.service.StatsService;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * com.lince.observer.math.service
 * Class StatsService
 * 14/03/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
@Service
public class StatsServiceImpl implements StatsService {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DataHubService dataHubService;
    private final LegacyConverterService legacyConverterService;


    @Autowired
    public StatsServiceImpl(DataHubService dataHubService, LegacyConverterService legacyConverterService) {
        this.dataHubService = dataHubService;
        this.legacyConverterService = legacyConverterService;
    }

    private LinceDkproAdapter getAdapter(UUID... uuids) {
        return new LinceDkproAdapter(getDataRegister(), getObservationTool(), uuids);
    }
    /**
     * Legacy method, using Lince 1 version
     *
     * @param firstRegister  valid uuid in register
     * @param secondRegister valid uuid in register
     * @return List of criteria + value
     */
    @Override
    public List<KeyValue<String, Double>> calculateLegacyKappa(UUID firstRegister, UUID secondRegister) {
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

    @Override
    public List<LinceRegisterWrapper> getDataRegister(){
        return dataHubService.getDataRegister();
    }

    @Override
    public List<Criteria> getObservationTool(){
        return dataHubService.getCriteria();
    }

    @Override
    public List<AgreementResult> calculateAgreementPercentage(UUID... selectedRegisters) {
        LinceDkproAdapter adapter = getAdapter(selectedRegisters);
        return adapter.calculatePercentageAgreement();
    }

    @Override
    public List<AgreementResult> calculateKrippendorf(UUID... selectedRegisters) {
        LinceDkproAdapter adapter = getAdapter(selectedRegisters);
        return adapter.calculateKrippendorfAlphaAgreement();
    }

    @Override
    public List<AgreementResult> getContingencyMatrix(UUID firstRegister, UUID secondRegister) {
        try {
            LinceDkproAdapter adapter = getAdapter(firstRegister, secondRegister);
            return adapter.getContingencyMatrix();
        } catch (Exception e) {
            log.error("StatsService for Kappa index via DKpro", e);
            return null;
        }
    }

    @Override
    public List<AgreementResult> calculateKappaPro(UUID firstRegister, UUID secondRegister) {
        try {
            LinceDkproAdapter adapter = getAdapter(firstRegister, secondRegister);
            return adapter.calculateKappaIndex();
        } catch (Exception e) {
            log.error("StatsService for Kappa index via DKpro", e);
            return null;
        }
    }
}
