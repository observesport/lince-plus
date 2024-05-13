package com.lince.observer.data.structure;

/**
 * coachgate-core-root
 * com.deicos.coachgate.core.data.structure
 * @author berto (alberto.soto@gmail.com)in 19/02/2016.
 * Description:
 */
public class BeanActionsConfig {
    String uriTemplate;
    String style;
    String type;
    String linkType;

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
