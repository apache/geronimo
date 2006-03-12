package org.apache.geronimo.naming.deployment.jsr88;

import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanLocatorType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;

/**
 * Represents an element of the gbean-locatorType in a Geronimo
 * deployment plan.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class GBeanLocator extends XmlBeanSupport {
    public GBeanLocator() {
        super(null);
    }

    public GBeanLocator(GerGbeanLocatorType xmlObject) {
        super(xmlObject);
    }

    protected GerGbeanLocatorType getGBeanLocator() {
        return (GerGbeanLocatorType) getXmlObject();
    }

    // Must be public but should not be a JavaBean property -- sigh
    public boolean configured() {
        return getXmlObject() != null;
    }

    public void configure(GerGbeanLocatorType xml) {
        setXmlObject(xml);
    }

    public String getGBeanLink() {
        return getGBeanLocator().getGbeanLink();
    }

    public void setGBeanLink(String link) {
        GerGbeanLocatorType locator = getGBeanLocator();
        if(link != null) {
            if(locator.isSetPattern()) {
                Pattern oldPattern = buildPattern(locator.getPattern());
                locator.unsetPattern();
                pcs.firePropertyChange("objectNameComponents", oldPattern, null);
            }
        }
        String old = getGBeanLink();
        locator.setGbeanLink(link);
        pcs.firePropertyChange("GBeanLink", old, link);
    }


    public Pattern buildPattern(GerPatternType patternType) {
        Pattern group = new Pattern();
        group.setGroupId(patternType.getGroupId());
        group.setArtifactId(patternType.getArtifactId());
        group.setVersion(patternType.getVersion());
        group.setModule(patternType.getModule());
        group.setName(patternType.getName());
        return group.empty() ? null : group;
    }

    public void setPattern(Pattern group) {
        GerGbeanLocatorType locator = getGBeanLocator();
        if(group != null && !group.empty()) {
            if(locator.isSetGbeanLink()) {
                String temp = locator.getGbeanLink();
                locator.unsetGbeanLink();
                pcs.firePropertyChange("GBeanLink", temp, null);
            }
        }
        Pattern old = buildPattern(locator.getPattern());
            locator.unsetPattern();
        if(group != null) {
            GerPatternType patternType = locator.addNewPattern();
            if(!isEmpty(group.getGroupId())) {
                patternType.setGroupId(group.getGroupId());
            }
            if(!isEmpty(group.getArtifactId())) {
                patternType.setArtifactId(group.getArtifactId());
            }
            if(!isEmpty(group.getModule())) {
                patternType.setModule(group.getModule());
            }
            if(!isEmpty(group.getName())) {
                patternType.setName(group.getName());
            }
            if(!isEmpty(group.getVersion())) {
                patternType.setVersion(group.getVersion());
            }
        }
        pcs.firePropertyChange("objectNameComponents", old, group);
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }
}
