package com.lince.observer.math.service;

import com.lince.observer.ai.agreement.LinceDkproAdapter;
import com.lince.observer.data.LegacyStatsHelper;
import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.KeyValue;
import com.lince.observer.data.bean.agreement.AgreementResult;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.service.StatsService;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.data.transfer.legacy.LegacyProject;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
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
    private final LegacyBridge legacyBridge;


    @Autowired
    public StatsServiceImpl(DataHubService dataHubService, LegacyBridge legacyBridge) {
        this.dataHubService = dataHubService;
        this.legacyBridge = legacyBridge;
    }

    private LinceDkproAdapter getAdapter(UUID... uuids) {
        return new LinceDkproAdapter(getDataRegister(), getObservationTool(), uuids);
    }
    /**
     * Legacy method, using Lince 1 version.
     * Both registers are converted by {@link LegacyBridge#toLegacy}, the single modern-to-legacy converter, and the
     * conversions and the instrument read run under the {@link LegacyBridge} lock, so no other conversion replaces
     * the legacy singletons in between (LO-349). Each conversion rebuilds the instrument from the same criteria and
     * starts a new register; the first register stays valid because legacy criteria compare by name.
     *
     * @param firstRegister  valid uuid in register
     * @param secondRegister valid uuid in register
     * @return List of criteria + value
     */
    @Override
    public List<KeyValue<String, Double>> calculateLegacyKappa(UUID firstRegister, UUID secondRegister) {
        return legacyBridge.withLegacyModel(() -> calculateLegacyKappaLocked(firstRegister, secondRegister));
    }

    private List<KeyValue<String, Double>> calculateLegacyKappaLocked(UUID firstRegister, UUID secondRegister) {
        try {
            List<KeyValue<String, Double>> response = new ArrayList<>();
            ILinceProject project = currentProjectSnapshot();
            Registro old1 = legacyBridge.toLegacy(new ExportSelection(project, firstRegister, List.of())).register();
            LegacyProject second = legacyBridge.toLegacy(new ExportSelection(project, secondRegister, List.of()));
            Registro old2 = second.register();
            Criterio[] criteriaList = second.instrument().getCriterios();
            List<Object> items = new ArrayList<>();
            CollectionUtils.addAll(items, (Object[]) criteriaList);
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

    /**
     * A snapshot of the loaded project as the bridge expects it: copies of the data hub criteria and register lists.
     */
    private ILinceProject currentProjectSnapshot() {
        ILinceProject project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>(dataHubService.getCriteria()));
        project.setRegister(new ArrayList<>(dataHubService.getDataRegister()));
        return project;
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
