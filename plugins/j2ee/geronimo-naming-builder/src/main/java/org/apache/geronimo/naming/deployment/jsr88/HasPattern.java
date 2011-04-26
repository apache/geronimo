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

import javax.xml.namespace.QName;
import org.apache.geronimo.deployment.dconfigbean.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlObjectBase;

/**
 * Represents an element in a Geronimo dployment plan that has a child
 * of type Pattern.  This handles patterns that are a member of a choice as
 * well as singleton patterns.
 *                                     <p>
 * Has 1 JavaBean Properties           <br />
 *  - pattern (type Pattern)           </p>
 *
 * @version $Rev$ $Date$
 */
public class HasPattern extends XmlBeanSupport {
    public HasPattern() {
        super(null);
    }

    public HasPattern(XmlObject xmlObject) {
        super(xmlObject);
    }

    /**
     * JavaBean getter for the Pattern property.  Gets a JavaBean of type
     * Pattern for the pattern child of this element, or null if there is no
     * pattern child.
     */
    public Pattern getPattern() {
        GerPatternType patternType = findPattern();
        if(patternType == null) return null;
        Pattern group = new Pattern();
        group.setGroupId(patternType.getGroupId());
        group.setArtifactId(patternType.getArtifactId());
        group.setVersion(patternType.getVersion());
        group.setModule(patternType.getModule());
        group.setName(patternType.getName());
        return group.empty() ? null : group;
    }

    /**
     * JavaBean setter for the Pattern property.  Calls the helper
     * clearNonPatternFromChoice if a non-null Pattern is set.
     */
    public void setPattern(Pattern group) {
        Pattern old = getPattern();
        if(group != null) {
            GerPatternType patternType;
            if(old == null) {
                patternType = (GerPatternType) ((XmlObjectBase)getXmlObject()).get_store().add_element_user(new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "pattern"));
            } else {
                patternType = findPattern();
            }
            if(!isEmpty(group.getGroupId())) {
                patternType.setGroupId(group.getGroupId());
            } else {
                if(patternType.isSetGroupId()) patternType.unsetGroupId();
            }
            if(!isEmpty(group.getArtifactId())) {
                patternType.setArtifactId(group.getArtifactId());
            } else {
                if(patternType.isSetArtifactId()) patternType.unsetArtifactId();
            }
            if(!isEmpty(group.getModule())) {
                patternType.setModule(group.getModule());
            } else {
                if(patternType.isSetModule()) patternType.unsetModule();
            }
            patternType.setName(group.getName());
            if(!isEmpty(group.getVersion())) {
                patternType.setVersion(group.getVersion());
            } else {
                if(patternType.isSetVersion()) patternType.unsetVersion();
            }
            clearNonPatternFromChoice();
        } else {
            if(old != null) {
                ((XmlObjectBase)getXmlObject()).get_store().remove_element(new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "pattern"), 0);
            }
        }
        pcs.firePropertyChange("objectNameComponents", old, group);
    }

    /**
     * Should be overridden to remove any non-pattern elements if this
     * element has a pattern that's part of a choice.  If this is called, it
     * means a non-null Pattern is in the process of being set.  This method
     * should fire property change events on any elements it removes.
     */
    protected void clearNonPatternFromChoice() {}

    /**
     * Should be called to remove any pattern child element if the pattern is
     * part of a choice and some other element in the choice was set to a
     * non-null value.  This will clear the pattern and send a property change
     * event on the "pattern" property if the pattern was set.
     */
    protected void clearPatternFromChoice() {
        Pattern pattern = getPattern();
        if(pattern != null) {
            ((XmlObjectBase)getXmlObject()).get_store().remove_element(new QName("http://geronimo.apache.org/xml/ns/naming-1.2", "pattern"), 0);
            pcs.firePropertyChange("pattern", pattern, null);
        }
    }

    /**
     * Gets the pattern child of this element, or null if there is none.
     */
    protected GerPatternType findPattern() {
        XmlObject[] patterns = getXmlObject().selectChildren(new QName(GerPatternType.type.getName().getNamespaceURI(), "pattern"));
        if(patterns.length == 0) {
            return null;
        }
        return (GerPatternType)patterns[0];
    }
}
