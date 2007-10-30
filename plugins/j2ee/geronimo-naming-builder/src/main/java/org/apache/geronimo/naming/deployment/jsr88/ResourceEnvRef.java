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
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;

/**
 * Represents an element of the resource-env-refType in a Geronimo deployment
 * plan.
 *                                             <p>
 * Has 4 JavaBean Properties                   <br />
 *  - refName (type String)                    <br />
 *  - pattern (type Pattern)                   <br />
 *  - adminObjectModule (type String)          <br />
 *  - adminObjectLink (type String)            </p>
 *
 * @version $Rev$ $Date$
 */
public class ResourceEnvRef extends HasPattern {
    public ResourceEnvRef() {
        super(null);
    }

    public ResourceEnvRef(GerResourceEnvRefType xmlObject) {
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

    public String getAdminObjectLink() {
        return getResourceRef().getAdminObjectLink();
    }

    public void setAdminObjectLink(String link) {
        GerResourceEnvRefType ref = getResourceRef();
        if(link != null && ref.isSetPattern()) {
            clearPatternFromChoice();
        }
        String old = getAdminObjectLink();
        ref.setAdminObjectLink(link);
        pcs.firePropertyChange("adminObjectLink", old, link);
    }

    public String getAdminObjectModule() {
        return getResourceRef().getAdminObjectModule();
    }

    public void setAdminObjectModule(String module) {
        GerResourceEnvRefType ref = getResourceRef();
        if(module != null && ref.isSetPattern()) {
            clearPatternFromChoice();
        }
        String old = getAdminObjectModule();
        ref.setAdminObjectModule(module);
        pcs.firePropertyChange("adminObjectModule", old, module);
    }


    protected void clearNonPatternFromChoice() {
        GerResourceEnvRefType ref = getResourceRef();
        if(ref.isSetAdminObjectLink()) {
            String temp = ref.getAdminObjectLink();
            ref.unsetAdminObjectLink();
            pcs.firePropertyChange("adminObjectLink", temp, null);
        }
        if(ref.isSetAdminObjectModule()) {
            String temp = ref.getAdminObjectModule();
            ref.unsetAdminObjectModule();
            pcs.firePropertyChange("adminObjectModule", temp, null);
        }
    }

    protected GerResourceEnvRefType getResourceRef() {
        return (GerResourceEnvRefType) getXmlObject();
    }

    public void configure(GerResourceEnvRefType xml) {
        setXmlObject(xml);
    }
}
