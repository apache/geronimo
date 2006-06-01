package org.apache.geronimo.naming.deployment.jsr88;

import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanLocatorType;

/**
 * Represents an element of the gbean-locatorType in a Geronimo
 * deployment plan.
 *                                     <p>
 * Has 2 JavaBean Properties           <br />
 *  - GBeanLink (type String)          <br />
 *  - pattern (type Pattern)           </p>
 *
 * @version $Rev$ $Date$
 */
public class GBeanLocator extends HasPattern {
    public GBeanLocator() {
        super(null);
    }

    public GBeanLocator(GerGbeanLocatorType xmlObject) {
        super(xmlObject);
    }

    protected GerGbeanLocatorType getGBeanLocator() {
        return (GerGbeanLocatorType) getXmlObject();
    }

    public void configure(GerGbeanLocatorType xml) {
        setXmlObject(xml);
    }

    public String getGBeanLink() {
        return getGBeanLocator().getGbeanLink();
    }

    public void setGBeanLink(String link) {
        GerGbeanLocatorType locator = getGBeanLocator();
        if(link != null && locator.isSetPattern()) {
            clearPatternFromChoice();
        }
        String old = getGBeanLink();
        locator.setGbeanLink(link);
        pcs.firePropertyChange("GBeanLink", old, link);
    }


    protected void clearNonPatternFromChoice() {
        GerGbeanLocatorType locator = getGBeanLocator();
        if(locator.isSetGbeanLink()) {
            String temp = locator.getGbeanLink();
            locator.unsetGbeanLink();
            pcs.firePropertyChange("GBeanLink", temp, null);
        }
    }
}
