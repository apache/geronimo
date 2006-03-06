/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.xmlbeans.XmlObject;

import javax.management.ObjectName;
import java.beans.PropertyEditor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
public class GBeanBuilder {
    private final GBeanData gbean;
    private final ClassLoader classLoader;
    private final DeploymentContext context;
    private final J2eeContext j2eeContext;
    private final Map xmlAttributeBuilderMap;
    private final Map xmlReferenceBuilderMap;

    GBeanBuilder(ObjectName objectName, GBeanInfo gBeanInfo, ClassLoader classLoader, DeploymentContext context, J2eeContext j2eeContext, Map xmlAttributeBuilderMap, Map xmlReferenceBuilderMap) {

        this.classLoader = classLoader;
        this.context = context;
        this.j2eeContext = j2eeContext;
        this.gbean = new GBeanData(objectName, gBeanInfo);
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
                    throw new DeploymentException("Unknown attribute " + name + " on " + gbean.getName());
                }
                type = attribute.getType();
            }

            PropertyEditor editor = PropertyEditors.findEditor(type, classLoader);
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

    public void setXmlAttribute(String name, XmlObject xmlObject) throws DeploymentException {
        String namespace = xmlObject.getDomNode().getNamespaceURI();
        XmlAttributeBuilder builder = (XmlAttributeBuilder) xmlAttributeBuilderMap.get(namespace);
        if (builder == null) {
            throw new DeploymentException("No attribute builder deployed for namespace: " + namespace);
        }
        GAttributeInfo attribute = gbean.getGBeanInfo().getAttribute(name);
        if (attribute == null) {
            throw new DeploymentException("Unknown attribute " + name + " on " + gbean.getName());
        }
        String type = attribute.getType();
        Object value = builder.getValue(xmlObject, type, classLoader);
        gbean.setAttribute(name, value);
    }

    public void setXmlReference(String name, XmlObject xmlObject) throws DeploymentException {
        String namespace = xmlObject.getDomNode().getNamespaceURI();
        XmlReferenceBuilder builder = (XmlReferenceBuilder) xmlReferenceBuilderMap.get(namespace);
        if (builder == null) {
            throw new DeploymentException("No reference builder deployed for namespace: " + namespace);
        }
        Set references = builder.getReferences(xmlObject, context, j2eeContext, classLoader);
        if (references != null && !references.isEmpty()) {
            gbean.setReferencePatterns(name, references);
        }
    }

    public void setReference(String name, ReferenceType pattern, J2eeContext j2eeContext) throws DeploymentException {
        setReference(name, new PatternType[]{pattern}, j2eeContext);
    }

    public void setReference(String name, PatternType[] patterns, J2eeContext j2eeContext) throws DeploymentException {
        Set patternNames = new HashSet(patterns.length);
        for (int i = 0; i < patterns.length; i++) {
            patternNames.add(buildUnresolvedReferenceInfo(name, patterns[i]));
        }
        gbean.setReferencePatterns(name, patternNames);
    }

    public void addDependency(PatternType patternType, J2eeContext j2eeContext) throws DeploymentException {
        AbstractNameQuery refInfo = buildUnresolvedReferenceInfo(null, patternType);
        gbean.getDependencies().add(refInfo);
    }

    private AbstractNameQuery buildUnresolvedReferenceInfo(String refName, PatternType pattern) throws DeploymentException {
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
            throw new DeploymentException("No reference named " + refName + " in gbean " + gbean.getName());
        }

        String groupId = pattern.isSetGroupId() ? pattern.getGroupId().trim() : null;
        String artifactid = pattern.isSetArtifactId() ? pattern.getArtifactId().trim() : null;
        String version = pattern.isSetVersion() ? pattern.getVersion().trim() : null;
        String module = pattern.isSetModule() ? pattern.getModule().trim() : null;
        String type = pattern.isSetType() ? pattern.getType().trim() : null;
        String name = pattern.getName().trim();

        List artifacts = Collections.EMPTY_LIST;
        if (artifactid != null) {
            artifacts = Collections.singletonList(new Artifact(groupId, artifactid, version, "car"));
        }
        //get the type from the gbean info if not supplied explicitly
        if (type == null) {
            type = referenceInfo.getNameTypeName();
        }
        Map nameMap = new HashMap();
        nameMap.put("name", name);
        if (type != null) {
            nameMap.put("type", type);
        }
        if (module != null) {
            nameMap.put("module", module);
        }
        String interfaceType = referenceInfo.getProxyType();
        return new AbstractNameQuery(artifacts, nameMap, Collections.singleton(interfaceType));
    }

    public GBeanData getGBeanData() {
        return gbean;
    }

}
