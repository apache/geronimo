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
package org.apache.geronimo.xml.ns.deployment;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * 
 *             Schema for Geronimo Services deployment plans.
 *             Instance documents should begin with the element:
 * 
 *             &gt;gbeans xmlns="http://geronimo.apache.org/xml/ns/deployment"&lt;
 *         
 * <!-- end-model-doc -->
 * @see org.apache.geronimo.xml.ns.deployment.DeploymentFactory
 * @model kind="package"
 * @generated
 */
public interface DeploymentPackage extends EPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * The package name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNAME = "deployment";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "http://geronimo.apache.org/xml/ns/deployment";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "deployment";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    DeploymentPackage eINSTANCE = org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl.init();

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.deployment.impl.AttributeTypeImpl <em>Attribute Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.deployment.impl.AttributeTypeImpl
     * @see org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl#getAttributeType()
     * @generated
     */
    int ATTRIBUTE_TYPE = 0;

    /**
     * The feature id for the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ATTRIBUTE_TYPE__VALUE = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ATTRIBUTE_TYPE__NAME = 1;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ATTRIBUTE_TYPE__TYPE = 2;

    /**
     * The number of structural features of the the '<em>Attribute Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ATTRIBUTE_TYPE_FEATURE_COUNT = 3;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.deployment.impl.ConfigurationTypeImpl <em>Configuration Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.deployment.impl.ConfigurationTypeImpl
     * @see org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl#getConfigurationType()
     * @generated
     */
    int CONFIGURATION_TYPE = 1;

    /**
     * The feature id for the '<em><b>Include</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIGURATION_TYPE__INCLUDE = 0;

    /**
     * The feature id for the '<em><b>Dependency</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIGURATION_TYPE__DEPENDENCY = 1;

    /**
     * The feature id for the '<em><b>Gbean</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIGURATION_TYPE__GBEAN = 2;

    /**
     * The feature id for the '<em><b>Config Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIGURATION_TYPE__CONFIG_ID = 3;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIGURATION_TYPE__DOMAIN = 4;

    /**
     * The feature id for the '<em><b>Parent Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIGURATION_TYPE__PARENT_ID = 5;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIGURATION_TYPE__SERVER = 6;

    /**
     * The number of structural features of the the '<em>Configuration Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIGURATION_TYPE_FEATURE_COUNT = 7;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.deployment.impl.DependencyTypeImpl <em>Dependency Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.deployment.impl.DependencyTypeImpl
     * @see org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl#getDependencyType()
     * @generated
     */
    int DEPENDENCY_TYPE = 2;

    /**
     * The feature id for the '<em><b>Group Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEPENDENCY_TYPE__GROUP_ID = 0;

    /**
     * The feature id for the '<em><b>Artifact Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEPENDENCY_TYPE__ARTIFACT_ID = 1;

    /**
     * The feature id for the '<em><b>Version</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEPENDENCY_TYPE__VERSION = 2;

    /**
     * The feature id for the '<em><b>Uri</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEPENDENCY_TYPE__URI = 3;

    /**
     * The number of structural features of the the '<em>Dependency Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEPENDENCY_TYPE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.deployment.impl.DocumentRootImpl <em>Document Root</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.deployment.impl.DocumentRootImpl
     * @see org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl#getDocumentRoot()
     * @generated
     */
    int DOCUMENT_ROOT = 3;

    /**
     * The feature id for the '<em><b>Mixed</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__MIXED = 0;

    /**
     * The feature id for the '<em><b>XMLNS Prefix Map</b></em>' map.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__XMLNS_PREFIX_MAP = 1;

    /**
     * The feature id for the '<em><b>XSI Schema Location</b></em>' map.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__XSI_SCHEMA_LOCATION = 2;

    /**
     * The feature id for the '<em><b>Configuration</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__CONFIGURATION = 3;

    /**
     * The feature id for the '<em><b>Dependency</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__DEPENDENCY = 4;

    /**
     * The feature id for the '<em><b>Gbean</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__GBEAN = 5;

    /**
     * The number of structural features of the the '<em>Document Root</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT_FEATURE_COUNT = 6;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl <em>Gbean Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.deployment.impl.GbeanTypeImpl
     * @see org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl#getGbeanType()
     * @generated
     */
    int GBEAN_TYPE = 4;

    /**
     * The feature id for the '<em><b>Group</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE__GROUP = 0;

    /**
     * The feature id for the '<em><b>Attribute</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE__ATTRIBUTE = 1;

    /**
     * The feature id for the '<em><b>Xml Attribute</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE__XML_ATTRIBUTE = 2;

    /**
     * The feature id for the '<em><b>Reference</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE__REFERENCE = 3;

    /**
     * The feature id for the '<em><b>References</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE__REFERENCES = 4;

    /**
     * The feature id for the '<em><b>Xml Reference</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE__XML_REFERENCE = 5;

    /**
     * The feature id for the '<em><b>Class</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE__CLASS = 6;

    /**
     * The feature id for the '<em><b>Gbean Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE__GBEAN_NAME = 7;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE__NAME = 8;

    /**
     * The number of structural features of the the '<em>Gbean Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_TYPE_FEATURE_COUNT = 9;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl <em>Pattern Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.deployment.impl.PatternTypeImpl
     * @see org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl#getPatternType()
     * @generated
     */
    int PATTERN_TYPE = 5;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATTERN_TYPE__DOMAIN = 0;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATTERN_TYPE__SERVER = 1;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATTERN_TYPE__APPLICATION = 2;

    /**
     * The feature id for the '<em><b>Module Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATTERN_TYPE__MODULE_TYPE = 3;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATTERN_TYPE__MODULE = 4;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATTERN_TYPE__TYPE = 5;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATTERN_TYPE__NAME = 6;

    /**
     * The feature id for the '<em><b>Gbean Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATTERN_TYPE__GBEAN_NAME = 7;

    /**
     * The number of structural features of the the '<em>Pattern Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATTERN_TYPE_FEATURE_COUNT = 8;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.deployment.impl.ReferencesTypeImpl <em>References Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.deployment.impl.ReferencesTypeImpl
     * @see org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl#getReferencesType()
     * @generated
     */
    int REFERENCES_TYPE = 6;

    /**
     * The feature id for the '<em><b>Pattern</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCES_TYPE__PATTERN = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCES_TYPE__NAME = 1;

    /**
     * The number of structural features of the the '<em>References Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCES_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.deployment.impl.ReferenceTypeImpl <em>Reference Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.deployment.impl.ReferenceTypeImpl
     * @see org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl#getReferenceType()
     * @generated
     */
    int REFERENCE_TYPE = 7;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE__DOMAIN = PATTERN_TYPE__DOMAIN;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE__SERVER = PATTERN_TYPE__SERVER;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE__APPLICATION = PATTERN_TYPE__APPLICATION;

    /**
     * The feature id for the '<em><b>Module Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE__MODULE_TYPE = PATTERN_TYPE__MODULE_TYPE;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE__MODULE = PATTERN_TYPE__MODULE;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE__TYPE = PATTERN_TYPE__TYPE;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE__NAME = PATTERN_TYPE__NAME;

    /**
     * The feature id for the '<em><b>Gbean Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE__GBEAN_NAME = PATTERN_TYPE__GBEAN_NAME;

    /**
     * The feature id for the '<em><b>Name1</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE__NAME1 = PATTERN_TYPE_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the the '<em>Reference Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REFERENCE_TYPE_FEATURE_COUNT = PATTERN_TYPE_FEATURE_COUNT + 1;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.deployment.impl.XmlAttributeTypeImpl <em>Xml Attribute Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.deployment.impl.XmlAttributeTypeImpl
     * @see org.apache.geronimo.xml.ns.deployment.impl.DeploymentPackageImpl#getXmlAttributeType()
     * @generated
     */
    int XML_ATTRIBUTE_TYPE = 8;

    /**
     * The feature id for the '<em><b>Any</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int XML_ATTRIBUTE_TYPE__ANY = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int XML_ATTRIBUTE_TYPE__NAME = 1;

    /**
     * The number of structural features of the the '<em>Xml Attribute Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int XML_ATTRIBUTE_TYPE_FEATURE_COUNT = 2;


    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.deployment.AttributeType <em>Attribute Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Attribute Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.AttributeType
     * @generated
     */
    EClass getAttributeType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.AttributeType#getValue <em>Value</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Value</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.AttributeType#getValue()
     * @see #getAttributeType()
     * @generated
     */
    EAttribute getAttributeType_Value();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.AttributeType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.AttributeType#getName()
     * @see #getAttributeType()
     * @generated
     */
    EAttribute getAttributeType_Name();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.AttributeType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.AttributeType#getType()
     * @see #getAttributeType()
     * @generated
     */
    EAttribute getAttributeType_Type();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType <em>Configuration Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Configuration Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ConfigurationType
     * @generated
     */
    EClass getConfigurationType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getInclude <em>Include</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Include</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ConfigurationType#getInclude()
     * @see #getConfigurationType()
     * @generated
     */
    EReference getConfigurationType_Include();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getDependency <em>Dependency</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Dependency</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ConfigurationType#getDependency()
     * @see #getConfigurationType()
     * @generated
     */
    EReference getConfigurationType_Dependency();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getGbean <em>Gbean</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Gbean</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ConfigurationType#getGbean()
     * @see #getConfigurationType()
     * @generated
     */
    EReference getConfigurationType_Gbean();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getConfigId <em>Config Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Config Id</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ConfigurationType#getConfigId()
     * @see #getConfigurationType()
     * @generated
     */
    EAttribute getConfigurationType_ConfigId();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ConfigurationType#getDomain()
     * @see #getConfigurationType()
     * @generated
     */
    EAttribute getConfigurationType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getParentId <em>Parent Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Parent Id</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ConfigurationType#getParentId()
     * @see #getConfigurationType()
     * @generated
     */
    EAttribute getConfigurationType_ParentId();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.ConfigurationType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ConfigurationType#getServer()
     * @see #getConfigurationType()
     * @generated
     */
    EAttribute getConfigurationType_Server();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.deployment.DependencyType <em>Dependency Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Dependency Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DependencyType
     * @generated
     */
    EClass getDependencyType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getGroupId <em>Group Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Group Id</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DependencyType#getGroupId()
     * @see #getDependencyType()
     * @generated
     */
    EAttribute getDependencyType_GroupId();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getArtifactId <em>Artifact Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Artifact Id</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DependencyType#getArtifactId()
     * @see #getDependencyType()
     * @generated
     */
    EAttribute getDependencyType_ArtifactId();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getVersion <em>Version</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Version</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DependencyType#getVersion()
     * @see #getDependencyType()
     * @generated
     */
    EAttribute getDependencyType_Version();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.DependencyType#getUri <em>Uri</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Uri</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DependencyType#getUri()
     * @see #getDependencyType()
     * @generated
     */
    EAttribute getDependencyType_Uri();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.deployment.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Document Root</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DocumentRoot
     * @generated
     */
    EClass getDocumentRoot();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.deployment.DocumentRoot#getMixed <em>Mixed</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Mixed</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DocumentRoot#getMixed()
     * @see #getDocumentRoot()
     * @generated
     */
    EAttribute getDocumentRoot_Mixed();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.deployment.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DocumentRoot#getXMLNSPrefixMap()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XMLNSPrefixMap();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.deployment.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XSI Schema Location</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DocumentRoot#getXSISchemaLocation()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XSISchemaLocation();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.deployment.DocumentRoot#getConfiguration <em>Configuration</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Configuration</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DocumentRoot#getConfiguration()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_Configuration();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.deployment.DocumentRoot#getDependency <em>Dependency</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Dependency</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DocumentRoot#getDependency()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_Dependency();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.deployment.DocumentRoot#getGbean <em>Gbean</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Gbean</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.DocumentRoot#getGbean()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_Gbean();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.deployment.GbeanType <em>Gbean Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Gbean Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType
     * @generated
     */
    EClass getGbeanType();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getGroup <em>Group</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Group</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType#getGroup()
     * @see #getGbeanType()
     * @generated
     */
    EAttribute getGbeanType_Group();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getAttribute <em>Attribute</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Attribute</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType#getAttribute()
     * @see #getGbeanType()
     * @generated
     */
    EReference getGbeanType_Attribute();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getXmlAttribute <em>Xml Attribute</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Xml Attribute</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType#getXmlAttribute()
     * @see #getGbeanType()
     * @generated
     */
    EReference getGbeanType_XmlAttribute();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getReference <em>Reference</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Reference</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType#getReference()
     * @see #getGbeanType()
     * @generated
     */
    EReference getGbeanType_Reference();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getReferences <em>References</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>References</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType#getReferences()
     * @see #getGbeanType()
     * @generated
     */
    EReference getGbeanType_References();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getXmlReference <em>Xml Reference</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Xml Reference</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType#getXmlReference()
     * @see #getGbeanType()
     * @generated
     */
    EReference getGbeanType_XmlReference();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getClass_ <em>Class</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Class</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType#getClass_()
     * @see #getGbeanType()
     * @generated
     */
    EAttribute getGbeanType_Class();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getGbeanName <em>Gbean Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Gbean Name</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType#getGbeanName()
     * @see #getGbeanType()
     * @generated
     */
    EAttribute getGbeanType_GbeanName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.GbeanType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.GbeanType#getName()
     * @see #getGbeanType()
     * @generated
     */
    EAttribute getGbeanType_Name();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.deployment.PatternType <em>Pattern Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Pattern Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType
     * @generated
     */
    EClass getPatternType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType#getDomain()
     * @see #getPatternType()
     * @generated
     */
    EAttribute getPatternType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType#getServer()
     * @see #getPatternType()
     * @generated
     */
    EAttribute getPatternType_Server();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType#getApplication()
     * @see #getPatternType()
     * @generated
     */
    EAttribute getPatternType_Application();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getModuleType <em>Module Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType#getModuleType()
     * @see #getPatternType()
     * @generated
     */
    EAttribute getPatternType_ModuleType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType#getModule()
     * @see #getPatternType()
     * @generated
     */
    EAttribute getPatternType_Module();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType#getType()
     * @see #getPatternType()
     * @generated
     */
    EAttribute getPatternType_Type();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType#getName()
     * @see #getPatternType()
     * @generated
     */
    EAttribute getPatternType_Name();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.PatternType#getGbeanName <em>Gbean Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Gbean Name</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.PatternType#getGbeanName()
     * @see #getPatternType()
     * @generated
     */
    EAttribute getPatternType_GbeanName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.deployment.ReferencesType <em>References Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>References Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ReferencesType
     * @generated
     */
    EClass getReferencesType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.deployment.ReferencesType#getPattern <em>Pattern</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Pattern</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ReferencesType#getPattern()
     * @see #getReferencesType()
     * @generated
     */
    EReference getReferencesType_Pattern();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.ReferencesType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ReferencesType#getName()
     * @see #getReferencesType()
     * @generated
     */
    EAttribute getReferencesType_Name();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.deployment.ReferenceType <em>Reference Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Reference Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ReferenceType
     * @generated
     */
    EClass getReferenceType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.ReferenceType#getName1 <em>Name1</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name1</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.ReferenceType#getName1()
     * @see #getReferenceType()
     * @generated
     */
    EAttribute getReferenceType_Name1();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.deployment.XmlAttributeType <em>Xml Attribute Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Xml Attribute Type</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.XmlAttributeType
     * @generated
     */
    EClass getXmlAttributeType();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.deployment.XmlAttributeType#getAny <em>Any</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Any</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.XmlAttributeType#getAny()
     * @see #getXmlAttributeType()
     * @generated
     */
    EAttribute getXmlAttributeType_Any();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.deployment.XmlAttributeType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.deployment.XmlAttributeType#getName()
     * @see #getXmlAttributeType()
     * @generated
     */
    EAttribute getXmlAttributeType_Name();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    DeploymentFactory getDeploymentFactory();

} //DeploymentPackage
