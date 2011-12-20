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

import java.beans.PropertyEditorManager;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.xbeans.AttributeType;
import org.apache.geronimo.deployment.xbeans.GbeanDocument;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.deployment.xbeans.ReferencesType;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.deployment.xbeans.XmlAttributeType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.MultiGBeanInfoFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class GBeanBuilder implements NamespaceDrivenBuilder {
    protected final Map<String, XmlAttributeBuilder> attrRefMap = new HashMap<String, XmlAttributeBuilder>();
    protected final Map<String, XmlReferenceBuilder> refRefMap = new HashMap<String, XmlReferenceBuilder>();
    private final GBeanInfoFactory infoFactory = new MultiGBeanInfoFactory();
    public static final QName SERVICE_QNAME = ServiceDocument.type.getDocumentElementName();
    private static final QName GBEAN_QNAME = GbeanDocument.type.getDocumentElementName();
    private static final QNameSet GBEAN_QNAME_SET = QNameSet.singleton(GBEAN_QNAME);

    //TODO why is this here?
    static {
        PropertyEditorManager.registerEditor(Environment.class, EnvironmentBuilder.class);
    }
    public GBeanBuilder() {
        bindXmlAttributeBuilder(new EnvironmentBuilder());
    }

    public void bindXmlAttributeBuilder(XmlAttributeBuilder xmlAttributeBuilder) {
        attrRefMap.put(xmlAttributeBuilder.getNamespace(), xmlAttributeBuilder);
    }

    public void unbindXmlAttributeBuilder(XmlAttributeBuilder xmlAttributeBuilder) {
        attrRefMap.remove(xmlAttributeBuilder.getNamespace());
    }

    public void bindXmlReferenceBuilder(XmlReferenceBuilder xmlReferenceBuilder) {
        refRefMap.put(xmlReferenceBuilder.getNamespace(), xmlReferenceBuilder);
    }

    public void unbindXmlReferenceBuilder(XmlReferenceBuilder xmlReferenceBuilder) {
        refRefMap.remove(xmlReferenceBuilder.getNamespace());
    }

    public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
    }

    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        XmlObject[] items = container.selectChildren(GBEAN_QNAME_SET);
        for (int i = 0; i < items.length; i++) {
            GbeanType gbean;
            try {
                gbean = (GbeanType) XmlBeansUtil.typedCopy(items[i], GbeanType.type);
            } catch (XmlException e) {
                throw new DeploymentException("Could not validate gbean xml", e);
            }
            addGBeanData(gbean, moduleContext.getModuleName(), moduleContext.getDeploymentBundle(), moduleContext);
        }
    }

    private AbstractName addGBeanData(GbeanType gbean, AbstractName moduleName, Bundle bundle, DeploymentContext context) throws DeploymentException {
        GBeanInfo gBeanInfo = infoFactory.getGBeanInfo(gbean.getClass1(), bundle);
        String namePart = gbean.getName();
        String j2eeType = gBeanInfo.getJ2eeType();
        AbstractName abstractName = context.getNaming().createChildName(moduleName, namePart, j2eeType);
        SingleGBeanBuilder builder = new SingleGBeanBuilder(abstractName, gBeanInfo, bundle, context, moduleName, attrRefMap , refRefMap);

        // set up attributes
        AttributeType[] attributeArray = gbean.getAttributeArray();
        if (attributeArray != null) {
            for (int j = 0; j < attributeArray.length; j++) {
                builder.setAttribute(attributeArray[j].getName().trim(), attributeArray[j].getType(), attributeArray[j].getStringValue());
            }
        }

        XmlAttributeType[] xmlAttributeArray = gbean.getXmlAttributeArray();
        if (xmlAttributeArray != null) {
            for (int i = 0; i < xmlAttributeArray.length; i++) {
                XmlAttributeType xmlAttributeType = xmlAttributeArray[i];
                String name = xmlAttributeType.getName().trim();
                XmlObject[] anys = xmlAttributeType.selectChildren(XmlAttributeType.type.qnameSetForWildcardElements());
                if (anys.length != 1) {
                    throw new DeploymentException("Unexpected count of xs:any elements in xml-attribute " + anys.length + " qnameset: " + XmlAttributeType.type.qnameSetForWildcardElements());
                }
                builder.setXmlAttribute(name, anys[0], xmlAttributeType);
            }
        }

        // set up all single pattern references
        ReferenceType[] referenceArray = gbean.getReferenceArray();
        if (referenceArray != null) {
            for (int j = 0; j < referenceArray.length; j++) {
                builder.setReference(referenceArray[j].getName2(), referenceArray[j], moduleName);
            }
        }

        // set up app multi-patterned references
        ReferencesType[] referencesArray = gbean.getReferencesArray();
        if (referencesArray != null) {
            for (int j = 0; j < referencesArray.length; j++) {
                builder.setReference(referencesArray[j].getName(), referencesArray[j].getPatternArray(), moduleName);
            }
        }

        XmlAttributeType[] xmlReferenceArray = gbean.getXmlReferenceArray();
        if (xmlReferenceArray != null) {
            for (int i = 0; i < xmlReferenceArray.length; i++) {
                XmlAttributeType xmlAttributeType = xmlReferenceArray[i];
                String name = xmlAttributeType.getName().trim();
                XmlObject[] anys = xmlAttributeType.selectChildren(XmlAttributeType.type.qnameSetForWildcardElements());
                if (anys.length != 1) {
                    throw new DeploymentException("Unexpected count of xs:any elements in xml-attribute " + anys.length + " qnameset: " + XmlAttributeType.type.qnameSetForWildcardElements());
                }
                builder.setXmlReference(name, anys[0]);
            }
        }

        PatternType[] dependencyArray = gbean.getDependencyArray();
        if (dependencyArray != null) {
            for (int i = 0; i < dependencyArray.length; i++) {
                PatternType patternType = dependencyArray[i];
                builder.addDependency(patternType);
            }
        }

        GBeanData gbeanData = builder.getGBeanData();
        try {
            context.addGBean(gbeanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException(e);
        }
        return abstractName;
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return GBEAN_QNAME_SET;
    }

    public QName getBaseQName() {
        return SERVICE_QNAME;
    }

}
