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

import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;

/**
 * Represents an element of the ejb-local-refType in a Geronimo deployment
 * plan.
 *                                          <p>
 * Has 3 JavaBean Properties                <br />
 *  - refName (type String)                 <br />
 *  - pattern (type Pattern)                <br />
 *  - ejbLink (type String)                 </p>
 *
 * @version $Rev$ $Date$
 */
public class EjbLocalRef extends HasPattern {
    public EjbLocalRef() {
        super(null);
    }

    public EjbLocalRef(GerEjbLocalRefType xmlObject) {
        super(xmlObject);
    }

    public void setRefName(String name) {
        String old = getEjbLocalRef().getRefName();
        getEjbLocalRef().setRefName(name);
        pcs.firePropertyChange("refName", old, name);
    }

    public String getRefName() {
        return getEjbLocalRef().getRefName();
    }

    public String getEjbLink() {
        return getEjbLocalRef().getEjbLink();
    }

    public void setEjbLink(String link) {
        GerEjbLocalRefType ref = getEjbLocalRef();
        if(link != null && ref.isSetPattern()) {
            clearPatternFromChoice();
        }
        String old = getEjbLink();
        ref.setEjbLink(link);
        pcs.firePropertyChange("ejbLink", old, link);
    }


    protected void clearNonPatternFromChoice() {
        GerEjbLocalRefType ref = getEjbLocalRef();
        if(ref.isSetEjbLink()) {
            String temp = ref.getEjbLink();
            ref.unsetEjbLink();
            pcs.firePropertyChange("ejbLink", temp, null);
        }
        // todo: clear CORBA property
    }

    // todo: getter and setter for CORBA property

    protected GerEjbLocalRefType getEjbLocalRef() {
        return (GerEjbLocalRefType) getXmlObject();
    }

    public void configure(GerEjbLocalRefType xml) {
        setXmlObject(xml);
    }
}
