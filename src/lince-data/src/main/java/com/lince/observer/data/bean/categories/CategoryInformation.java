package com.lince.observer.data.bean.categories;

import com.lince.observer.data.LinceDataConstants;

/**
 * com.lince.observer.data.bean.categories
 * Class CategoryInformation
 * 01/11/2019
 * Specific node for storing info
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class CategoryInformation extends Category {
    /**
     * Gets
     * @param parentId parent id
     * @return random int from the id of the parent
     */
    Integer generateId(Integer parentId) {
        return parentId * LinceDataConstants.CATEGORY_INFO_ID_MULTIPLIER;
    }

    public CategoryInformation(Criteria parent, String value) {
        super();
        this.id = generateId(parent.id);
        this.parent = parent.id;
        this.nodeInformation = value;
        this.name = parent.name;
        // same code as parent to get proper value while searching
        this.code = parent.code;
    }
}