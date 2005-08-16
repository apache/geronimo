/**
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
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
package org.apache.geronimo.xml.ns.deployment.impl;

import org.apache.geronimo.xml.ns.deployment.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DeploymentFactoryImpl extends EFactoryImpl implements DeploymentFactory {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DeploymentFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case DeploymentPackage.ATTRIBUTE_TYPE: return createAttributeType();
            case DeploymentPackage.CONFIGURATION_TYPE: return createConfigurationType();
            case DeploymentPackage.DEPENDENCY_TYPE: return createDependencyType();
            case DeploymentPackage.DOCUMENT_ROOT: return createDocumentRoot();
            case DeploymentPackage.GBEAN_TYPE: return createGbeanType();
            case DeploymentPackage.PATTERN_TYPE: return createPatternType();
            case DeploymentPackage.REFERENCES_TYPE: return createReferencesType();
            case DeploymentPackage.REFERENCE_TYPE: return createReferenceType();
            case DeploymentPackage.XML_ATTRIBUTE_TYPE: return createXmlAttributeType();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public AttributeType createAttributeType() {
        AttributeTypeImpl attributeType = new AttributeTypeImpl();
        return attributeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ConfigurationType createConfigurationType() {
        ConfigurationTypeImpl configurationType = new ConfigurationTypeImpl();
        return configurationType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DependencyType createDependencyType() {
        DependencyTypeImpl dependencyType = new DependencyTypeImpl();
        return dependencyType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DocumentRoot createDocumentRoot() {
        DocumentRootImpl documentRoot = new DocumentRootImpl();
        return documentRoot;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GbeanType createGbeanType() {
        GbeanTypeImpl gbeanType = new GbeanTypeImpl();
        return gbeanType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PatternType createPatternType() {
        PatternTypeImpl patternType = new PatternTypeImpl();
        return patternType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ReferencesType createReferencesType() {
        ReferencesTypeImpl referencesType = new ReferencesTypeImpl();
        return referencesType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ReferenceType createReferenceType() {
        ReferenceTypeImpl referenceType = new ReferenceTypeImpl();
        return referenceType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public XmlAttributeType createXmlAttributeType() {
        XmlAttributeTypeImpl xmlAttributeType = new XmlAttributeTypeImpl();
        return xmlAttributeType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DeploymentPackage getDeploymentPackage() {
        return (DeploymentPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    public static DeploymentPackage getPackage() {
        return DeploymentPackage.eINSTANCE;
    }

} //DeploymentFactoryImpl
