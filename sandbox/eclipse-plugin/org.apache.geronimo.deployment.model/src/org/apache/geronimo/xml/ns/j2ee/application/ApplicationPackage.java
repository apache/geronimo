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
package org.apache.geronimo.xml.ns.j2ee.application;

import java.lang.String;

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
 * 
 *    See http://www.w3.org/XML/1998/namespace.html and
 *    http://www.w3.org/TR/REC-xml for information about this namespace.
 * 
 *     This schema document describes the XML namespace, in a form
 *     suitable for import by other schema documents.  
 * 
 *     Note that local names in this namespace are intended to be defined
 *     only by the World Wide Web Consortium or its subgroups.  The
 *     following names are currently defined in this namespace and should
 *     not be used with conflicting semantics by any Working Group,
 *     specification, or document instance:
 * 
 *     base (as an attribute name): denotes an attribute whose value
 *          provides a URI to be used as the base for interpreting any
 *          relative URIs in the scope of the element on which it
 *          appears; its value is inherited.  This name is reserved
 *          by virtue of its definition in the XML Base specification.
 * 
 *     id   (as an attribute name): denotes an attribute whose value
 *          should be interpreted as if declared to be of type ID.
 *          The xml:id specification is not yet a W3C Recommendation,
 *          but this attribute is included here to facilitate experimentation
 *          with the mechanisms it proposes.  Note that it is _not_ included
 *          in the specialAttrs attribute group.
 * 
 *     lang (as an attribute name): denotes an attribute whose value
 *          is a language code for the natural language of the content of
 *          any element; its value is inherited.  This name is reserved
 *          by virtue of its definition in the XML specification.
 *   
 *     space (as an attribute name): denotes an attribute whose
 *          value is a keyword indicating what whitespace processing
 *          discipline is intended for the content of the element; its
 *          value is inherited.  This name is reserved by virtue of its
 *          definition in the XML specification.
 * 
 *     Father (in any context at all): denotes Jon Bosak, the chair of 
 *          the original XML Working Group.  This name is reserved by 
 *          the following decision of the W3C XML Plenary and 
 *          XML Coordination groups:
 * 
 *              In appreciation for his vision, leadership and dedication
 *              the W3C XML Plenary on this 10th day of February, 2000
 *              reserves for Jon Bosak in perpetuity the XML name
 *              xml:Father
 *   
 * This schema defines attributes and an attribute group
 *         suitable for use by
 *         schemas wishing to allow xml:base, xml:lang or xml:space attributes
 *         on elements they define.
 * 
 *         To enable this, such a schema must import this schema
 *         for the XML namespace, e.g. as follows:
 *         &lt;schema . . .&gt;
 *          . . .
 *          &lt;import namespace="http://www.w3.org/XML/1998/namespace"
 *                     schemaLocation="http://www.w3.org/2001/03/xml.xsd"/&gt;
 * 
 *         Subsequently, qualified reference to any of the attributes
 *         or the group defined below will have the desired effect, e.g.
 * 
 *         &lt;type . . .&gt;
 *          . . .
 *          &lt;attributeGroup ref="xml:specialAttrs"/&gt;
 *  
 *          will define a type which will schema-validate an instance
 *          element with any of those attributes
 * In keeping with the XML Schema WG's standard versioning
 *    policy, this schema document will persist at
 *    http://www.w3.org/2004/10/xml.xsd.
 *    At the date of issue it can also be found at
 *    http://www.w3.org/2001/xml.xsd.
 *    The schema document at that URI may however change in the future,
 *    in order to remain compatible with the latest version of XML Schema
 *    itself, or with the XML namespace itself.  In other words, if the XML
 *    Schema or XML namespaces change, the version of this document at
 *    http://www.w3.org/2001/xml.xsd will change
 *    accordingly; the version at
 *    http://www.w3.org/2004/10/xml.xsd will not change.
 *   
 * <!-- end-model-doc -->
 * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationFactory
 * @model kind="package"
 * @generated
 */
public interface ApplicationPackage extends EPackage {
    /**
     * The package name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNAME = "application";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "http://geronimo.apache.org/xml/ns/j2ee/application";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "application";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    ApplicationPackage eINSTANCE = org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationPackageImpl.init();

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl <em>Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationTypeImpl
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationPackageImpl#getApplicationType()
     * @generated
     */
    int APPLICATION_TYPE = 0;

    /**
     * The feature id for the '<em><b>Dependency</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int APPLICATION_TYPE__DEPENDENCY = 0;

    /**
     * The feature id for the '<em><b>Module</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int APPLICATION_TYPE__MODULE = 1;

    /**
     * The feature id for the '<em><b>Ext Module</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int APPLICATION_TYPE__EXT_MODULE = 2;

    /**
     * The feature id for the '<em><b>Security</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int APPLICATION_TYPE__SECURITY = 3;

    /**
     * The feature id for the '<em><b>Gbean</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int APPLICATION_TYPE__GBEAN = 4;

    /**
     * The feature id for the '<em><b>Application Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int APPLICATION_TYPE__APPLICATION_NAME = 5;

    /**
     * The feature id for the '<em><b>Config Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int APPLICATION_TYPE__CONFIG_ID = 6;

    /**
     * The feature id for the '<em><b>Parent Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int APPLICATION_TYPE__PARENT_ID = 7;

    /**
     * The number of structural features of the the '<em>Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int APPLICATION_TYPE_FEATURE_COUNT = 8;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.j2ee.application.impl.DocumentRootImpl <em>Document Root</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.DocumentRootImpl
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationPackageImpl#getDocumentRoot()
     * @generated
     */
    int DOCUMENT_ROOT = 1;

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
     * The feature id for the '<em><b>Application</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__APPLICATION = 3;

    /**
     * The number of structural features of the the '<em>Document Root</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ExtModuleTypeImpl <em>Ext Module Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.ExtModuleTypeImpl
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationPackageImpl#getExtModuleType()
     * @generated
     */
    int EXT_MODULE_TYPE = 2;

    /**
     * The feature id for the '<em><b>Connector</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EXT_MODULE_TYPE__CONNECTOR = 0;

    /**
     * The feature id for the '<em><b>Ejb</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EXT_MODULE_TYPE__EJB = 1;

    /**
     * The feature id for the '<em><b>Java</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EXT_MODULE_TYPE__JAVA = 2;

    /**
     * The feature id for the '<em><b>Web</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EXT_MODULE_TYPE__WEB = 3;

    /**
     * The feature id for the '<em><b>Internal Path</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EXT_MODULE_TYPE__INTERNAL_PATH = 4;

    /**
     * The feature id for the '<em><b>External Path</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EXT_MODULE_TYPE__EXTERNAL_PATH = 5;

    /**
     * The feature id for the '<em><b>Any</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EXT_MODULE_TYPE__ANY = 6;

    /**
     * The number of structural features of the the '<em>Ext Module Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EXT_MODULE_TYPE_FEATURE_COUNT = 7;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.j2ee.application.impl.ModuleTypeImpl <em>Module Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.ModuleTypeImpl
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationPackageImpl#getModuleType()
     * @generated
     */
    int MODULE_TYPE = 3;

    /**
     * The feature id for the '<em><b>Connector</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MODULE_TYPE__CONNECTOR = 0;

    /**
     * The feature id for the '<em><b>Ejb</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MODULE_TYPE__EJB = 1;

    /**
     * The feature id for the '<em><b>Java</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MODULE_TYPE__JAVA = 2;

    /**
     * The feature id for the '<em><b>Web</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MODULE_TYPE__WEB = 3;

    /**
     * The feature id for the '<em><b>Alt Dd</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MODULE_TYPE__ALT_DD = 4;

    /**
     * The feature id for the '<em><b>Any</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MODULE_TYPE__ANY = 5;

    /**
     * The number of structural features of the the '<em>Module Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MODULE_TYPE_FEATURE_COUNT = 6;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.j2ee.application.impl.StringImpl <em>String</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.StringImpl
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationPackageImpl#getString()
     * @generated
     */
    int STRING = 5;

    /**
     * The feature id for the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int STRING__VALUE = 0;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int STRING__ID = 1;

    /**
     * The number of structural features of the the '<em>String</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int STRING_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.j2ee.application.impl.PathTypeImpl <em>Path Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.PathTypeImpl
     * @see org.apache.geronimo.xml.ns.j2ee.application.impl.ApplicationPackageImpl#getPathType()
     * @generated
     */
    int PATH_TYPE = 4;

    /**
     * The feature id for the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATH_TYPE__VALUE = STRING__VALUE;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATH_TYPE__ID = STRING__ID;

    /**
     * The number of structural features of the the '<em>Path Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PATH_TYPE_FEATURE_COUNT = STRING_FEATURE_COUNT + 0;


    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationType
     * @generated
     */
    EClass getApplicationType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getDependency <em>Dependency</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Dependency</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getDependency()
     * @see #getApplicationType()
     * @generated
     */
    EReference getApplicationType_Dependency();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getModule()
     * @see #getApplicationType()
     * @generated
     */
    EReference getApplicationType_Module();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getExtModule <em>Ext Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ext Module</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getExtModule()
     * @see #getApplicationType()
     * @generated
     */
    EReference getApplicationType_ExtModule();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getSecurity <em>Security</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Security</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getSecurity()
     * @see #getApplicationType()
     * @generated
     */
    EReference getApplicationType_Security();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getGbean <em>Gbean</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Gbean</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getGbean()
     * @see #getApplicationType()
     * @generated
     */
    EReference getApplicationType_Gbean();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getApplicationName <em>Application Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application Name</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getApplicationName()
     * @see #getApplicationType()
     * @generated
     */
    EAttribute getApplicationType_ApplicationName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getConfigId <em>Config Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Config Id</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getConfigId()
     * @see #getApplicationType()
     * @generated
     */
    EAttribute getApplicationType_ConfigId();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getParentId <em>Parent Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Parent Id</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationType#getParentId()
     * @see #getApplicationType()
     * @generated
     */
    EAttribute getApplicationType_ParentId();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Document Root</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot
     * @generated
     */
    EClass getDocumentRoot();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot#getMixed <em>Mixed</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Mixed</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot#getMixed()
     * @see #getDocumentRoot()
     * @generated
     */
    EAttribute getDocumentRoot_Mixed();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot#getXMLNSPrefixMap()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XMLNSPrefixMap();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XSI Schema Location</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot#getXSISchemaLocation()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XSISchemaLocation();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot#getApplication()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_Application();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType <em>Ext Module Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Ext Module Type</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType
     * @generated
     */
    EClass getExtModuleType();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getConnector <em>Connector</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Connector</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getConnector()
     * @see #getExtModuleType()
     * @generated
     */
    EReference getExtModuleType_Connector();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getEjb <em>Ejb</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Ejb</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getEjb()
     * @see #getExtModuleType()
     * @generated
     */
    EReference getExtModuleType_Ejb();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getJava <em>Java</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Java</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getJava()
     * @see #getExtModuleType()
     * @generated
     */
    EReference getExtModuleType_Java();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getWeb <em>Web</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Web</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getWeb()
     * @see #getExtModuleType()
     * @generated
     */
    EReference getExtModuleType_Web();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getInternalPath <em>Internal Path</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Internal Path</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getInternalPath()
     * @see #getExtModuleType()
     * @generated
     */
    EAttribute getExtModuleType_InternalPath();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getExternalPath <em>External Path</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>External Path</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getExternalPath()
     * @see #getExtModuleType()
     * @generated
     */
    EAttribute getExtModuleType_ExternalPath();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getAny <em>Any</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Any</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ExtModuleType#getAny()
     * @see #getExtModuleType()
     * @generated
     */
    EAttribute getExtModuleType_Any();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType <em>Module Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Module Type</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ModuleType
     * @generated
     */
    EClass getModuleType();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getConnector <em>Connector</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Connector</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getConnector()
     * @see #getModuleType()
     * @generated
     */
    EReference getModuleType_Connector();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getEjb <em>Ejb</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Ejb</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getEjb()
     * @see #getModuleType()
     * @generated
     */
    EReference getModuleType_Ejb();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getJava <em>Java</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Java</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getJava()
     * @see #getModuleType()
     * @generated
     */
    EReference getModuleType_Java();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getWeb <em>Web</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Web</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getWeb()
     * @see #getModuleType()
     * @generated
     */
    EReference getModuleType_Web();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getAltDd <em>Alt Dd</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Alt Dd</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getAltDd()
     * @see #getModuleType()
     * @generated
     */
    EReference getModuleType_AltDd();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getAny <em>Any</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Any</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.ModuleType#getAny()
     * @see #getModuleType()
     * @generated
     */
    EAttribute getModuleType_Any();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.j2ee.application.PathType <em>Path Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Path Type</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.PathType
     * @generated
     */
    EClass getPathType();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.j2ee.application.String <em>String</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>String</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.String
     * @generated
     */
    EClass getString();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.j2ee.application.String#getValue <em>Value</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Value</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.String#getValue()
     * @see #getString()
     * @generated
     */
    EAttribute getString_Value();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.j2ee.application.String#getId <em>Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Id</em>'.
     * @see org.apache.geronimo.xml.ns.j2ee.application.String#getId()
     * @see #getString()
     * @generated
     */
    EAttribute getString_Id();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    ApplicationFactory getApplicationFactory();

} //ApplicationPackage
