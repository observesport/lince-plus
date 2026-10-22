package com.lince.observer.data.transfer;

import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportOutcomeTest {

    @Test
    void ofCountsTheTargetProject() {
        LinceFileProjectWrapper target = new LinceFileProjectWrapper();
        target.setObservationTool(new ArrayList<>(List.of(new Criteria(), new Criteria())));
        target.setRegister(new ArrayList<>(List.of(new LinceRegisterWrapper())));

        ImportOutcome outcome = ImportOutcome.of(target, 7, List.of("w"));

        assertEquals(new ImportOutcome(2, 1, 7, 0, List.of("w")), outcome);
    }

    @Test
    void ofTreatsMissingListsAsEmpty() {
        LinceFileProjectWrapper target = new LinceFileProjectWrapper();
        target.setObservationTool(null);
        target.setRegister(null);
        target.setProfiles(null);

        assertEquals(new ImportOutcome(0, 0, 0, 0, List.of()), ImportOutcome.of(target, 0, null));
    }
}
