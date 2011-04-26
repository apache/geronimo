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

import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;

/**
 * Represents an element of the message-destinationType in a Geronimo deployment
 * plan.
 *                                             <p>
 * Has 4 JavaBean Properties                   <br />
 *  - messageDestinationName (type String)     <br />
 *  - pattern (type Pattern)                   <br />
 *  - adminObjectModule (type String)          <br />
 *  - adminObjectLink (type String)            </p>
 *
 * @version $Rev$ $Date$
 */
public class MessageDestination extends HasPattern {
    public MessageDestination() {
        super(null);
    }

    public MessageDestination(GerMessageDestinationType xmlObject) {
        super(xmlObject);
    }

    public void setMessageDestinationName(String name) {
        String old = getMessageDestination().getMessageDestinationName();
        getMessageDestination().setMessageDestinationName(name);
        pcs.firePropertyChange("messageDestinationName", old, name);
    }

    public String getMessageDestinationName() {
        return getMessageDestination().getMessageDestinationName();
    }

    public String getAdminObjectLink() {
        return getMessageDestination().getAdminObjectLink();
    }

    public void setAdminObjectLink(String link) {
        GerMessageDestinationType ref = getMessageDestination();
        if(link != null && ref.isSetPattern()) {
            clearPatternFromChoice();
        }
        String old = getAdminObjectLink();
        ref.setAdminObjectLink(link);
        pcs.firePropertyChange("adminObjectLink", old, link);
    }

    public String getAdminObjectModule() {
        return getMessageDestination().getAdminObjectModule();
    }

    public void setAdminObjectModule(String module) {
        GerMessageDestinationType ref = getMessageDestination();
        if(module != null && ref.isSetPattern()) {
            clearPatternFromChoice();
        }
        String old = getAdminObjectModule();
        ref.setAdminObjectModule(module);
        pcs.firePropertyChange("adminObjectModule", old, module);
    }


    protected void clearNonPatternFromChoice() {
        GerMessageDestinationType ref = getMessageDestination();
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

    protected GerMessageDestinationType getMessageDestination() {
        return (GerMessageDestinationType) getXmlObject();
    }

    public void configure(GerMessageDestinationType xml) {
        setXmlObject(xml);
    }
}
