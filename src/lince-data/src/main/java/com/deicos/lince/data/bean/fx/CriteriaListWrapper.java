package com.deicos.lince.data.bean.fx;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * lince-scientific-base
 * com.deicos.lince.data
 * @author berto (alberto.soto@gmail.com)in 28/02/2017.
 * Description:
 */
@XmlRootElement(name = "criterias")
public class CriteriaListWrapper {
    private List<Criteria> criteria;

    @XmlElement(name = "criteria")
    public List<Criteria> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<Criteria> criteria) {
        this.criteria = criteria;
    }
}
