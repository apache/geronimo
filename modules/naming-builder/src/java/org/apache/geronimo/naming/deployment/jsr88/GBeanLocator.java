package org.apache.geronimo.naming.deployment.jsr88;

import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanLocatorType;
import org.apache.xmlbeans.XmlObject;

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
            ObjectNameGroup before = null;
            if(locator.isSetApplication()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetApplication();
            }
            if(locator.isSetDomain()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetDomain();
            }
            if(locator.isSetModule()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetModule();
            }
            if(locator.isSetName()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetName();
            }
            if(locator.isSetServer()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetServer();
            }
            if(locator.isSetType()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetType();
            }
            if(before != null) {
                pcs.firePropertyChange("objectNameComponents", before, null);
            }
            if(locator.isSetTargetName()) {
                String temp = locator.getTargetName();
                locator.unsetTargetName();
                pcs.firePropertyChange("targetName", temp, null);
            }
        }
        String old = getGBeanLink();
        locator.setGbeanLink(link);
        pcs.firePropertyChange("GBeanLink", old, link);
    }

    public String getTargetName() {
        return getGBeanLocator().getGbeanLink();
    }

    public void setTargetName(String name) {
        GerGbeanLocatorType locator = getGBeanLocator();
        if(name != null) {
            ObjectNameGroup before = null;
            if(locator.isSetApplication()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetApplication();
            }
            if(locator.isSetDomain()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetDomain();
            }
            if(locator.isSetModule()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetModule();
            }
            if(locator.isSetName()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetName();
            }
            if(locator.isSetServer()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetServer();
            }
            if(locator.isSetType()) {
                if(before == null) before = getObjectNameComponents();
                locator.unsetType();
            }
            if(before != null) {
                pcs.firePropertyChange("objectNameComponents", before, null);
            }
            if(locator.isSetGbeanLink()) {
                String temp = locator.getGbeanLink();
                locator.unsetGbeanLink();
                pcs.firePropertyChange("GBeanLink", temp, null);
            }
        }
        String old = getTargetName();
        locator.setTargetName(name);
        pcs.firePropertyChange("targetName", old, name);
    }

    public ObjectNameGroup getObjectNameComponents() {
        ObjectNameGroup group = new ObjectNameGroup();
        GerGbeanLocatorType locator = getGBeanLocator();
        group.setApplication(locator.getApplication());
        group.setDomain(locator.getDomain());
        group.setModule(locator.getModule());
        group.setName(locator.getName());
        group.setServer(locator.getServer());
        group.setType(locator.getType());
        return group.empty() ? null : group;
    }

    public void setObjectNamecomponents(ObjectNameGroup group) {
        GerGbeanLocatorType locator = getGBeanLocator();
        if(group != null && !group.empty()) {
            if(locator.isSetGbeanLink()) {
                String temp = locator.getGbeanLink();
                locator.unsetGbeanLink();
                pcs.firePropertyChange("GBeanLink", temp, null);
            }
            if(locator.isSetTargetName()) {
                String temp = locator.getTargetName();
                locator.unsetTargetName();
                pcs.firePropertyChange("targetName", temp, null);
            }
        }
        ObjectNameGroup old = getObjectNameComponents();
        if(group == null) {
            locator.unsetApplication();
            locator.unsetDomain();
            locator.unsetModule();
            locator.unsetName();
            locator.unsetServer();
            locator.unsetType();
        } else {
            if(isEmpty(group.getApplication())) {
                locator.unsetApplication();
            } else {
                locator.setApplication(group.getApplication());
            }
            if(isEmpty(group.getDomain())) {
                locator.unsetDomain();
            } else {
                locator.setDomain(group.getDomain());
            }
            if(isEmpty(group.getModule())) {
                locator.unsetModule();
            } else {
                locator.setModule(group.getModule());
            }
            if(isEmpty(group.getName())) {
                locator.unsetName();
            } else {
                locator.setName(group.getName());
            }
            if(isEmpty(group.getServer())) {
                locator.unsetServer();
            } else {
                locator.setServer(group.getServer());
            }
            if(isEmpty(group.getType())) {
                locator.unsetType();
            } else {
                locator.setType(group.getType());
            }
        }
        pcs.firePropertyChange("objectNameComponents", old, getObjectNameComponents());
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }
}
