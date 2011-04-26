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

package org.apache.geronimo.deployment.service;

import java.beans.PropertyEditor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class SingleGBeanBuilder {
    private final GBeanData gbean;
    private final Bundle bundle;
    private final DeploymentContext context;
    private final AbstractName moduleName;
    private final Map xmlAttributeBuilderMap;
    private final Map xmlReferenceBuilderMap;

    SingleGBeanBuilder(AbstractName abstractName, GBeanInfo gBeanInfo, Bundle bundle, DeploymentContext context, AbstractName moduleName, Map xmlAttributeBuilderMap, Map xmlReferenceBuilderMap) {

        this.bundle = bundle;
        this.context = context;
        this.moduleName = moduleName;
        this.gbean = new GBeanData(abstractName, gBeanInfo);
        this.xmlAttributeBuilderMap = xmlAttributeBuilderMap;
        this.xmlReferenceBuilderMap = xmlReferenceBuilderMap;
    }

    public void setAttribute(String name, String type, String text) throws DeploymentException {
        if (text != null) {
            text = text.trim(); // avoid formatting errors due to extra whitespace in XML configuration file
        }
        try {
            // @todo we should not need all of common just for this
            if (type == null) {
                GAttributeInfo attribute = gbean.getGBeanInfo().getAttribute(name);
                if (attribute == null) {
                    throw new DeploymentException("Unknown attribute " + name + " on " + gbean.getAbstractName());
                }
                type = attribute.getType();
            }

            PropertyEditor editor = PropertyEditors.findEditor(type, bundle);
            if (editor == null) {
                throw new DeploymentException("Unable to find PropertyEditor for " + type);
            }
            editor.setAsText(text);
            Object value = editor.getValue();
            gbean.setAttribute(name, value);
        } catch (DeploymentException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Unable to find PropertyEditor for " + type, e);
        } catch (Exception e) {
            throw new DeploymentException("Unable to set attribute " + name + " to " + text, e);
        }
    }

    public void setXmlAttribute(String name, XmlObject xmlObject, XmlObject enclosing) throws DeploymentException {
        String namespace = xmlObject.getDomNode().getNamespaceURI();
        XmlAttributeBuilder builder = (XmlAttributeBuilder) xmlAttributeBuilderMap.get(namespace);
        if (builder == null) {
            throw new DeploymentException("No attribute builder deployed for namespace: " + namespace);
        }
        GAttributeInfo attribute = gbean.getGBeanInfo().getAttribute(name);
        if (attribute == null) {
            throw new DeploymentException("Unknown attribute " + name + " on " + gbean.getAbstractName());
        }
        String type = attribute.getType();
        Object value = builder.getValue(xmlObject, enclosing, type, bundle);
        gbean.setAttribute(name, value);
    }

    public void setXmlReference(String name, XmlObject xmlObject) throws DeploymentException {
        String namespace = xmlObject.getDomNode().getNamespaceURI();
        XmlReferenceBuilder builder = (XmlReferenceBuilder) xmlReferenceBuilderMap.get(namespace);
        if (builder == null) {
            throw new DeploymentException("No reference builder deployed for namespace: " + namespace);
        }
        ReferencePatterns references = builder.getReferences(xmlObject, context, moduleName, bundle);
        if (references != null) {
            gbean.setReferencePatterns(name, references);
        }
    }

    public void setReference(String name, ReferenceType pattern, AbstractName parentName) throws DeploymentException {
        setReference(name, new PatternType[]{pattern}, parentName);
    }

    public void setReference(String name, PatternType[] patterns, AbstractName parentName) throws DeploymentException {
        Set patternNames = new HashSet(patterns.length);
        for (int i = 0; i < patterns.length; i++) {
            patternNames.add(buildAbstractNameQuery(name, patterns[i]));
        }
        gbean.setReferencePatterns(name, patternNames);
    }

    public void addDependency(PatternType patternType) throws DeploymentException {
        AbstractNameQuery refInfo = buildAbstractNameQuery(patternType, null);
        gbean.addDependency(refInfo);
    }

    private AbstractNameQuery buildAbstractNameQuery(String refName, PatternType pattern) throws DeploymentException {
//        if (refName == null) {
//            throw new DeploymentException("No type specified in dependency pattern " + pattern + " for gbean " + gbean.getName());
//        }
        assert refName != null;
        GReferenceInfo referenceInfo = null;
        Set referenceInfos = gbean.getGBeanInfo().getReferences();
        for (Iterator iterator = referenceInfos.iterator(); iterator.hasNext();) {
            GReferenceInfo testReferenceInfo = (GReferenceInfo) iterator.next();
            String testRefName = testReferenceInfo.getName();
            if (testRefName.equals(refName)) {
                referenceInfo = testReferenceInfo;
            }
        }
        if (referenceInfo == null) {
            throw new DeploymentException("No reference named " + refName + " in gbean " + gbean.getAbstractName());
        }

        return buildAbstractNameQuery(pattern, referenceInfo);
    }

    public static AbstractNameQuery buildAbstractNameQuery(PatternType pattern, GReferenceInfo referenceInfo) {
        String nameTypeName = referenceInfo == null? null: referenceInfo.getNameTypeName();
        Set interfaceTypes = referenceInfo == null? null: Collections.singleton(referenceInfo.getReferenceType());
        return buildAbstractNameQuery(pattern, nameTypeName, interfaceTypes);
    }

    public static AbstractNameQuery buildAbstractNameQuery(PatternType pattern, String nameTypeName, Set interfaceTypes) {
        String groupId = pattern.isSetGroupId() ? pattern.getGroupId().trim() : null;
        String artifactid = pattern.isSetArtifactId() ? pattern.getArtifactId().trim() : null;
        String version = pattern.isSetVersion() ? pattern.getVersion().trim() : null;
        String module = pattern.isSetModule() ? pattern.getModule().trim() : null;
        String type = pattern.isSetType() ? pattern.getType().trim() : null;
        String name = pattern.isSetName() ? pattern.getName().trim() : null;

        Artifact artifact = artifactid != null? new Artifact(groupId, artifactid, version, "car"): null;
        //get the type from the gbean info if not supplied explicitly
        if (type == null) {
            type = nameTypeName;
        }
        Map nameMap = new HashMap();
        if (name != null) {
            nameMap.put("name", name);
        }
        if (type != null) {
            nameMap.put("j2eeType", type);
        }
        if (module != null) {
            nameMap.put("J2EEModule", module);
        }
        return new AbstractNameQuery(artifact, nameMap, interfaceTypes);
    }

    public GBeanData getGBeanData() {
        return gbean;
    }

}
