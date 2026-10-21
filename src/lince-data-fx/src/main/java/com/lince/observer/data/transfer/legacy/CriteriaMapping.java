package com.lince.observer.data.transfer.legacy;

import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Two-way link between modern {@link Criteria} and legacy {@link Criterio} built by {@link LegacyBridge} during a
 * conversion (LO-349). Lookups are by instance first: neither class has a hash code consistent with its
 * {@code equals} (Criteria compares code or id, Criterio compares name), so both directions use identity maps.
 * {@link #toModern(Criterio)} falls back to the legacy notion of identity, the criterion name, because legacy
 * register rows may hold detached {@code Criterio} copies.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public final class CriteriaMapping {

    private final Map<Criteria, Criterio> modernToLegacy = new IdentityHashMap<>();
    private final Map<Criterio, Criteria> legacyToModern = new IdentityHashMap<>();
    private final List<Criteria> modernOrder = new ArrayList<>();

    void put(Criteria modern, Criterio legacy) {
        modernToLegacy.put(modern, legacy);
        legacyToModern.put(legacy, modern);
        modernOrder.add(modern);
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

    /**
     * @param legacy a legacy criterion, converted or a detached copy with the same name
     * @return the modern criteria, null when none matches
     */
    public Criteria toModern(Criterio legacy) {
        Criteria modern = legacyToModern.get(legacy);
        if (modern != null || legacy == null) {
            return modern;
        }
        for (Map.Entry<Criterio, Criteria> entry : legacyToModern.entrySet()) {
            if (StringUtils.equals(entry.getKey().getNombre(), legacy.getNombre())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * @return converted modern criteria in conversion order
     */
    public List<Criteria> modernCriteria() {
        return Collections.unmodifiableList(modernOrder);
    }
}
