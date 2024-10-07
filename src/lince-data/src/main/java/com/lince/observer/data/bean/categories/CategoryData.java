package com.lince.observer.data.bean.categories;

import com.lince.observer.data.LinceDataConstants;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * lince-scientific-desktop
 * com.lince.observer.data
 *
 * @author berto (alberto.soto@gmail.com)in 29/02/2016.
 * Description:
 */
@XmlTransient //Prevents the mapping of a JavaBean property/type to XML representation
@XmlSeeAlso({Category.class, Criteria.class}) //Instructs JAXB to also bi
public abstract class CategoryData implements Serializable {
    protected Integer id;
    protected String code; // old version support
    protected String name;
    protected Integer value;
    protected String nodeInformation;
    protected String description;
    protected Integer level;
    protected String type;
    protected Integer parent; //FK
    protected String position; //action
    protected Integer related;//FK

    public static final String DEFAULT = "default";
    public static final String POSITION_BEFORE = "before";
    public static final String POSITION_AFTER = "after";
    public static final String POSITION_INNER_FIRST = "firstchildren";
    public static final String POSITION_INNER_LAST = "lastChild";

    public CategoryData() {
        super();
        this.type = DEFAULT;
        this.level = 0;
    }

    public CategoryData(Integer id, String name, Integer level, String code) {
        this.id = id;
        this.name = name;
        this.code = StringUtils.defaultIfEmpty(code, "cod-" + this.id);
        this.level = level == null ? 0 : level;
        this.type = DEFAULT;
    }

    public boolean isCategory() {
        return false;
    }

    /**
     * prefix for code used in visualization label generation
     *
     * @return CRI or CAT if criteria or category
     */
    public abstract String getCategoryDataPrefix();

    public CategoryData(Integer id, String name, Integer level) {
        this(id, name, level, null);
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getRelated() {
        return related;
    }

    public void setRelated(Integer related) {
        this.related = related;
    }

    public void setType(String type) {
        this.type = type;
        //TreeMap<Integer, CategoryData> m = new TreeMap<>();
        //m.put(1, new CategoryData());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNodeInformation() {
        return nodeInformation;
    }

    public void setNodeInformation(String nodeInformation) {
        this.nodeInformation = nodeInformation;
    }

    @Override
    public boolean equals(Object obj) {
        boolean eq = super.equals(obj);
        try {
            if (!eq && CategoryData.class.isAssignableFrom(obj.getClass())) {
                final CategoryData objItem = (CategoryData) obj;
                eq = (StringUtils.equals(objItem.getCode(), this.getCode())
                        || this.getId().equals(objItem.getId()));
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(this.getClass()).error("equals", e);
        }
        return eq;
    }

    @Override
    public String toString() {
        return code + '-' + name;
    }

    public static boolean isInformationNode(String code) {
        return StringUtils.endsWith(code, LinceDataConstants.CATEGORY_INFO_SUFIX);
    }

    public static String getInformationNodeCode(String code) {
        return StringUtils.substringBefore(code, LinceDataConstants.CATEGORY_INFO_SUFIX);
    }
}
