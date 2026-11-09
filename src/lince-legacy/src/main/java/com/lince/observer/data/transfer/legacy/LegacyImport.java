package com.lince.observer.data.transfer.legacy;

import com.lince.observer.data.bean.categories.Criteria;

import java.util.List;

/**
 * Result of converting a legacy instrument into modern criteria (LO-349).
 *
 * @param criteria converted criteria, ids from 1
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public record LegacyImport(List<Criteria> criteria) {

    public LegacyImport {
        criteria = List.copyOf(criteria);
    }
}
