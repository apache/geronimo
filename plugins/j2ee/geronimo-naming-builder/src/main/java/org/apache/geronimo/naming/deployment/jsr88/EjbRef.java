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

import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;

/**
 * Represents an element of the ejb-refType in a Geronimo deployment plan.
 *                                          <p>
 * Has 4 JavaBean Properties                <br />
 *  - refName (type String)                 <br />
 *  - pattern (type Pattern)                <br />
 *  - corbaNamingGroup (type ???)           <br />
 *  - ejbLink (type String)                 </p>
 *
 * @version $Rev$ $Date$
 */
public class EjbRef extends HasPattern {
    public EjbRef() {
        super(null);
    }

    public EjbRef(GerEjbRefType xmlObject) {
        super(xmlObject);
    }

    public void setRefName(String name) {
        String old = getEjbRef().getRefName();
        getEjbRef().setRefName(name);
        pcs.firePropertyChange("refName", old, name);
    }

    public String getRefName() {
        return getEjbRef().getRefName();
    }

    public String getEjbLink() {
        return getEjbRef().getEjbLink();
    }

    public void setEjbLink(String link) {
        GerEjbRefType ref = getEjbRef();
        if(link != null) {
            if(ref.isSetPattern()) {
                clearPatternFromChoice();
            }
            // todo: clear CORBA property
        }
        String old = getEjbLink();
        ref.setEjbLink(link);
        pcs.firePropertyChange("ejbLink", old, link);
    }


    protected void clearNonPatternFromChoice() {
        GerEjbRefType ref = getEjbRef();
        if(ref.isSetEjbLink()) {
            String temp = ref.getEjbLink();
            ref.unsetEjbLink();
            pcs.firePropertyChange("ejbLink", temp, null);
        }
        // todo: clear CORBA property
    }

    // todo: getter and setter for CORBA property

    protected GerEjbRefType getEjbRef() {
        return (GerEjbRefType) getXmlObject();
    }

    public void configure(GerEjbRefType xml) {
        setXmlObject(xml);
    }
}
