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

import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;

/**
 * Represents an element of the resource-refType in a Geronimo deployment
 * plan.
 *                                          <p>
 * Has 4 JavaBean Properties                <br />
 *  - refName (type String)                 <br />
 *  - pattern (type Pattern)                <br />
 *  - url (type String)                     <br />
 *  - resourceLink (type String)            </p>
 *
 * @version $Rev$ $Date$
 */
public class ResourceRef extends HasPattern {
    public ResourceRef() {
        super(null);
    }

    public ResourceRef(GerResourceRefType xmlObject) {
        super(xmlObject);
    }

    public void setRefName(String name) {
        String old = getResourceRef().getRefName();
        getResourceRef().setRefName(name);
        pcs.firePropertyChange("refName", old, name);
    }

    public String getRefName() {
        return getResourceRef().getRefName();
    }

    public String getResourceLink() {
        return getResourceRef().getResourceLink();
    }

    public void setResourceLink(String link) {
        GerResourceRefType ref = getResourceRef();
        if(link != null) {
            if(ref.isSetPattern()) {
                clearPatternFromChoice();
            }
            if(ref.isSetUrl()) {
                String old = getUrl();
                ref.unsetUrl();
                pcs.firePropertyChange("url", old, null);
            }
        }
        String old = getResourceLink();
        ref.setResourceLink(link);
        pcs.firePropertyChange("resourceLink", old, link);
    }

    public String getUrl() {
        return getResourceRef().getResourceLink();
    }

    public void setUrl(String link) {
        GerResourceRefType ref = getResourceRef();
        if(link != null) {
            if(ref.isSetPattern()) {
                clearPatternFromChoice();
            }
            if(ref.isSetResourceLink()) {
                String old = getResourceLink();
                ref.unsetResourceLink();
                pcs.firePropertyChange("resourceLink", old, null);
            }
        }
        String old = getUrl();
        ref.setUrl(link);
        pcs.firePropertyChange("url", old, link);
    }


    protected void clearNonPatternFromChoice() {
        GerResourceRefType ref = getResourceRef();
        if(ref.isSetResourceLink()) {
            String temp = ref.getResourceLink();
            ref.unsetResourceLink();
            pcs.firePropertyChange("resourceLink", temp, null);
        }
        if(ref.isSetUrl()) {
            String temp = ref.getUrl();
            ref.unsetUrl();
            pcs.firePropertyChange("url", temp, null);
        }
    }

    protected GerResourceRefType getResourceRef() {
        return (GerResourceRefType) getXmlObject();
    }

    public void configure(GerResourceRefType xml) {
        setXmlObject(xml);
    }
}
