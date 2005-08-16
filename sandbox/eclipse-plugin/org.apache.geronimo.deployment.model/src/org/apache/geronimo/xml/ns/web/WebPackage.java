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
package org.apache.geronimo.xml.ns.web;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
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
 * 
 *             
 *             Partial schema containing common naming elements which can be included in other schemas.
 *             
 *         
 * <!-- end-model-doc -->
 * @see org.apache.geronimo.xml.ns.web.WebFactory
 * @model kind="package"
 * @generated
 */
public interface WebPackage extends EPackage {
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
    String eNAME = "web";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "http://geronimo.apache.org/xml/ns/web";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "web";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    WebPackage eINSTANCE = org.apache.geronimo.xml.ns.web.impl.WebPackageImpl.init();

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.web.impl.ConfigParamTypeImpl <em>Config Param Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.web.impl.ConfigParamTypeImpl
     * @see org.apache.geronimo.xml.ns.web.impl.WebPackageImpl#getConfigParamType()
     * @generated
     */
    int CONFIG_PARAM_TYPE = 0;

    /**
     * The feature id for the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIG_PARAM_TYPE__VALUE = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIG_PARAM_TYPE__NAME = 1;

    /**
     * The number of structural features of the the '<em>Config Param Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONFIG_PARAM_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.web.impl.ContainerConfigTypeImpl <em>Container Config Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.web.impl.ContainerConfigTypeImpl
     * @see org.apache.geronimo.xml.ns.web.impl.WebPackageImpl#getContainerConfigType()
     * @generated
     */
    int CONTAINER_CONFIG_TYPE = 1;

    /**
     * The feature id for the '<em><b>Config Param</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONTAINER_CONFIG_TYPE__CONFIG_PARAM = 0;

    /**
     * The feature id for the '<em><b>Container</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONTAINER_CONFIG_TYPE__CONTAINER = 1;

    /**
     * The number of structural features of the the '<em>Container Config Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CONTAINER_CONFIG_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.web.impl.DocumentRootImpl <em>Document Root</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.web.impl.DocumentRootImpl
     * @see org.apache.geronimo.xml.ns.web.impl.WebPackageImpl#getDocumentRoot()
     * @generated
     */
    int DOCUMENT_ROOT = 2;

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
     * The feature id for the '<em><b>Web App</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__WEB_APP = 3;

    /**
     * The number of structural features of the the '<em>Document Root</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl <em>App Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.web.impl.WebAppTypeImpl
     * @see org.apache.geronimo.xml.ns.web.impl.WebPackageImpl#getWebAppType()
     * @generated
     */
    int WEB_APP_TYPE = 3;

    /**
     * The feature id for the '<em><b>Dependency</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__DEPENDENCY = 0;

    /**
     * The feature id for the '<em><b>Context Root</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__CONTEXT_ROOT = 1;

    /**
     * The feature id for the '<em><b>Context Priority Classloader</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__CONTEXT_PRIORITY_CLASSLOADER = 2;

    /**
     * The feature id for the '<em><b>Container Config</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__CONTAINER_CONFIG = 3;

    /**
     * The feature id for the '<em><b>Security Realm Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__SECURITY_REALM_NAME = 4;

    /**
     * The feature id for the '<em><b>Security</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__SECURITY = 5;

    /**
     * The feature id for the '<em><b>Ejb Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__EJB_REF = 6;

    /**
     * The feature id for the '<em><b>Ejb Local Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__EJB_LOCAL_REF = 7;

    /**
     * The feature id for the '<em><b>Service Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__SERVICE_REF = 8;

    /**
     * The feature id for the '<em><b>Resource Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__RESOURCE_REF = 9;

    /**
     * The feature id for the '<em><b>Resource Env Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__RESOURCE_ENV_REF = 10;

    /**
     * The feature id for the '<em><b>Gbean</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__GBEAN = 11;

    /**
     * The feature id for the '<em><b>Config Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__CONFIG_ID = 12;

    /**
     * The feature id for the '<em><b>Parent Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE__PARENT_ID = 13;

    /**
     * The number of structural features of the the '<em>App Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_APP_TYPE_FEATURE_COUNT = 14;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.web.WebContainerType <em>Container Type</em>}' enum.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.web.WebContainerType
     * @see org.apache.geronimo.xml.ns.web.impl.WebPackageImpl#getWebContainerType()
     * @generated
     */
    int WEB_CONTAINER_TYPE = 4;

    /**
     * The meta object id for the '<em>Container Type Object</em>' data type.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.web.WebContainerType
     * @see org.apache.geronimo.xml.ns.web.impl.WebPackageImpl#getWebContainerTypeObject()
     * @generated
     */
    int WEB_CONTAINER_TYPE_OBJECT = 5;


    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.web.ConfigParamType <em>Config Param Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Config Param Type</em>'.
     * @see org.apache.geronimo.xml.ns.web.ConfigParamType
     * @generated
     */
    EClass getConfigParamType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.web.ConfigParamType#getValue <em>Value</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Value</em>'.
     * @see org.apache.geronimo.xml.ns.web.ConfigParamType#getValue()
     * @see #getConfigParamType()
     * @generated
     */
    EAttribute getConfigParamType_Value();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.web.ConfigParamType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.web.ConfigParamType#getName()
     * @see #getConfigParamType()
     * @generated
     */
    EAttribute getConfigParamType_Name();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.web.ContainerConfigType <em>Container Config Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Container Config Type</em>'.
     * @see org.apache.geronimo.xml.ns.web.ContainerConfigType
     * @generated
     */
    EClass getContainerConfigType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.web.ContainerConfigType#getConfigParam <em>Config Param</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Config Param</em>'.
     * @see org.apache.geronimo.xml.ns.web.ContainerConfigType#getConfigParam()
     * @see #getContainerConfigType()
     * @generated
     */
    EReference getContainerConfigType_ConfigParam();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.web.ContainerConfigType#getContainer <em>Container</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Container</em>'.
     * @see org.apache.geronimo.xml.ns.web.ContainerConfigType#getContainer()
     * @see #getContainerConfigType()
     * @generated
     */
    EAttribute getContainerConfigType_Container();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.web.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Document Root</em>'.
     * @see org.apache.geronimo.xml.ns.web.DocumentRoot
     * @generated
     */
    EClass getDocumentRoot();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.web.DocumentRoot#getMixed <em>Mixed</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Mixed</em>'.
     * @see org.apache.geronimo.xml.ns.web.DocumentRoot#getMixed()
     * @see #getDocumentRoot()
     * @generated
     */
    EAttribute getDocumentRoot_Mixed();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.web.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
     * @see org.apache.geronimo.xml.ns.web.DocumentRoot#getXMLNSPrefixMap()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XMLNSPrefixMap();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.web.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XSI Schema Location</em>'.
     * @see org.apache.geronimo.xml.ns.web.DocumentRoot#getXSISchemaLocation()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XSISchemaLocation();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.web.DocumentRoot#getWebApp <em>Web App</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Web App</em>'.
     * @see org.apache.geronimo.xml.ns.web.DocumentRoot#getWebApp()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_WebApp();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.web.WebAppType <em>App Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>App Type</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType
     * @generated
     */
    EClass getWebAppType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.web.WebAppType#getDependency <em>Dependency</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Dependency</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getDependency()
     * @see #getWebAppType()
     * @generated
     */
    EReference getWebAppType_Dependency();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.web.WebAppType#getContextRoot <em>Context Root</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Context Root</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getContextRoot()
     * @see #getWebAppType()
     * @generated
     */
    EAttribute getWebAppType_ContextRoot();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.web.WebAppType#isContextPriorityClassloader <em>Context Priority Classloader</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Context Priority Classloader</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#isContextPriorityClassloader()
     * @see #getWebAppType()
     * @generated
     */
    EAttribute getWebAppType_ContextPriorityClassloader();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.web.WebAppType#getContainerConfig <em>Container Config</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Container Config</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getContainerConfig()
     * @see #getWebAppType()
     * @generated
     */
    EReference getWebAppType_ContainerConfig();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.web.WebAppType#getSecurityRealmName <em>Security Realm Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Security Realm Name</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getSecurityRealmName()
     * @see #getWebAppType()
     * @generated
     */
    EAttribute getWebAppType_SecurityRealmName();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.web.WebAppType#getSecurity <em>Security</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Security</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getSecurity()
     * @see #getWebAppType()
     * @generated
     */
    EReference getWebAppType_Security();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.web.WebAppType#getEjbRef <em>Ejb Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Ref</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getEjbRef()
     * @see #getWebAppType()
     * @generated
     */
    EReference getWebAppType_EjbRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.web.WebAppType#getEjbLocalRef <em>Ejb Local Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Local Ref</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getEjbLocalRef()
     * @see #getWebAppType()
     * @generated
     */
    EReference getWebAppType_EjbLocalRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.web.WebAppType#getServiceRef <em>Service Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Service Ref</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getServiceRef()
     * @see #getWebAppType()
     * @generated
     */
    EReference getWebAppType_ServiceRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.web.WebAppType#getResourceRef <em>Resource Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Resource Ref</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getResourceRef()
     * @see #getWebAppType()
     * @generated
     */
    EReference getWebAppType_ResourceRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.web.WebAppType#getResourceEnvRef <em>Resource Env Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Resource Env Ref</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getResourceEnvRef()
     * @see #getWebAppType()
     * @generated
     */
    EReference getWebAppType_ResourceEnvRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.web.WebAppType#getGbean <em>Gbean</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Gbean</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getGbean()
     * @see #getWebAppType()
     * @generated
     */
    EReference getWebAppType_Gbean();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.web.WebAppType#getConfigId <em>Config Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Config Id</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getConfigId()
     * @see #getWebAppType()
     * @generated
     */
    EAttribute getWebAppType_ConfigId();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.web.WebAppType#getParentId <em>Parent Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Parent Id</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebAppType#getParentId()
     * @see #getWebAppType()
     * @generated
     */
    EAttribute getWebAppType_ParentId();

    /**
     * Returns the meta object for enum '{@link org.apache.geronimo.xml.ns.web.WebContainerType <em>Container Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for enum '<em>Container Type</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebContainerType
     * @generated
     */
    EEnum getWebContainerType();

    /**
     * Returns the meta object for data type '{@link org.apache.geronimo.xml.ns.web.WebContainerType <em>Container Type Object</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for data type '<em>Container Type Object</em>'.
     * @see org.apache.geronimo.xml.ns.web.WebContainerType
     * @model instanceClass="org.apache.geronimo.xml.ns.web.WebContainerType"
     *        extendedMetaData="name='web-containerType:Object' baseType='web-containerType'" 
     * @generated
     */
    EDataType getWebContainerTypeObject();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    WebFactory getWebFactory();

} //WebPackage
