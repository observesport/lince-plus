package com.deicos.lince.ai.agreement;

import com.deicos.lince.data.bean.categories.Criteria;
import org.json.JSONArray;

/**
 * com.deicos.lince.ai.agreement
 * Class AgreementResult
 * 12/04/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class AgreementResult {
    private Criteria criteria;
    private Double agreement;
    private Double observedDisagreement;
    private Double expectedDisagreement;
    private AgreementResultType type;
    private JSONArray contingencyMatrix;

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public Double getAgreement() {
        return agreement;
    }

    public void setAgreement(Double agreement) {
        this.agreement = agreement;
    }

    public AgreementResultType getType() {
        return type;
    }

    public void setType(AgreementResultType type) {
        this.type = type;
    }

    public Double getObservedDisagreement() {
        return observedDisagreement;
    }

    public void setObservedDisagreement(Double observedDisagreement) {
        this.observedDisagreement = observedDisagreement;
    }

    public Double getExpectedDisagreement() {
        return expectedDisagreement;
    }

    public void setExpectedDisagreement(Double expectedDisagreement) {
        this.expectedDisagreement = expectedDisagreement;
    }

    public JSONArray getContingencyMatrix() {
        return contingencyMatrix;
    }

    public void setContingencyMatrix(JSONArray contingencyMatrix) {
        this.contingencyMatrix = contingencyMatrix;
    }
}
