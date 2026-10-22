package com.lince.observer.data.transfer.legacy;

import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Link from modern {@link Criteria} to the legacy {@link Criterio} built by {@link LegacyBridge} during an export
 * conversion (LO-349). Lookups are by instance: {@code Criteria} has no hash code consistent with its
 * {@code equals} (it compares code or id), so the map is an identity map.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public final class CriteriaMapping {

    private final Map<Criteria, Criterio> modernToLegacy = new IdentityHashMap<>();

    void put(Criteria modern, Criterio legacy) {
        modernToLegacy.put(modern, legacy);
    }

    /**
     * @param modern a criteria of the converted project
     * @return its legacy criterion, null when it was not converted
     */
    public Criterio toLegacy(Criteria modern) {
        return modernToLegacy.get(modern);
    }

    /**
     * @param selected criteria of the converted project, in the wanted order
     * @return the legacy criteria in the same order
     * @throws ExportException UNKNOWN_CRITERIA listing the ids of criteria that were not converted
     */
    public List<Criterio> toLegacy(List<Criteria> selected) {
        List<Criterio> result = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (Criteria criteria : selected) {
            Criterio legacy = toLegacy(criteria);
            if (legacy == null) {
                missing.add(String.valueOf(criteria.getId()));
            } else {
                result.add(legacy);
            }
        }
        if (!missing.isEmpty()) {
            throw new ExportException(TransferException.Reason.UNKNOWN_CRITERIA,
                    "Criteria not present in the legacy instrument: " + String.join(", ", missing), missing);
        }
        return result;
    }
}
