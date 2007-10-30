/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
