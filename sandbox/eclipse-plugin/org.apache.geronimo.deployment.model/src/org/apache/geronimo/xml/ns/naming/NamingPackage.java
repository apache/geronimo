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
package org.apache.geronimo.xml.ns.naming;

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
 *             
 *             Partial schema containing common naming elements which can be included in other schemas.
 *             
 *         
 * <!-- end-model-doc -->
 * @see org.apache.geronimo.xml.ns.naming.NamingFactory
 * @model kind="package"
 * @generated
 */
public interface NamingPackage extends EPackage {
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
    String eNAME = "naming";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "http://geronimo.apache.org/xml/ns/naming";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "naming";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    NamingPackage eINSTANCE = org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl.init();

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.CssTypeImpl <em>Css Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.CssTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getCssType()
     * @generated
     */
    int CSS_TYPE = 0;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CSS_TYPE__DOMAIN = 0;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CSS_TYPE__SERVER = 1;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CSS_TYPE__APPLICATION = 2;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CSS_TYPE__MODULE = 3;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CSS_TYPE__TYPE = 4;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CSS_TYPE__NAME = 5;

    /**
     * The number of structural features of the the '<em>Css Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CSS_TYPE_FEATURE_COUNT = 6;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl <em>Document Root</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.DocumentRootImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getDocumentRoot()
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
     * The feature id for the '<em><b>Cmp Connection Factory</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__CMP_CONNECTION_FACTORY = 3;

    /**
     * The feature id for the '<em><b>Ejb Local Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__EJB_LOCAL_REF = 4;

    /**
     * The feature id for the '<em><b>Ejb Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__EJB_REF = 5;

    /**
     * The feature id for the '<em><b>Resource Adapter</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__RESOURCE_ADAPTER = 6;

    /**
     * The feature id for the '<em><b>Resource Env Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__RESOURCE_ENV_REF = 7;

    /**
     * The feature id for the '<em><b>Resource Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__RESOURCE_REF = 8;

    /**
     * The feature id for the '<em><b>Service Ref</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__SERVICE_REF = 9;

    /**
     * The feature id for the '<em><b>Workmanager</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__WORKMANAGER = 10;

    /**
     * The number of structural features of the the '<em>Document Root</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT_FEATURE_COUNT = 11;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.EjbLocalRefTypeImpl <em>Ejb Local Ref Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.EjbLocalRefTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getEjbLocalRefType()
     * @generated
     */
    int EJB_LOCAL_REF_TYPE = 2;

    /**
     * The feature id for the '<em><b>Ref Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE__REF_NAME = 0;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE__DOMAIN = 1;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE__SERVER = 2;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE__APPLICATION = 3;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE__MODULE = 4;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE__TYPE = 5;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE__NAME = 6;

    /**
     * The feature id for the '<em><b>Ejb Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE__EJB_LINK = 7;

    /**
     * The feature id for the '<em><b>Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE__TARGET_NAME = 8;

    /**
     * The number of structural features of the the '<em>Ejb Local Ref Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_LOCAL_REF_TYPE_FEATURE_COUNT = 9;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.EjbRefTypeImpl <em>Ejb Ref Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.EjbRefTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getEjbRefType()
     * @generated
     */
    int EJB_REF_TYPE = 3;

    /**
     * The feature id for the '<em><b>Ref Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__REF_NAME = 0;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__DOMAIN = 1;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__SERVER = 2;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__APPLICATION = 3;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__MODULE = 4;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__TYPE = 5;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__NAME = 6;

    /**
     * The feature id for the '<em><b>Ns Corbaloc</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__NS_CORBALOC = 7;

    /**
     * The feature id for the '<em><b>Name1</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__NAME1 = 8;

    /**
     * The feature id for the '<em><b>Css</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__CSS = 9;

    /**
     * The feature id for the '<em><b>Css Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__CSS_LINK = 10;

    /**
     * The feature id for the '<em><b>Css Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__CSS_NAME = 11;

    /**
     * The feature id for the '<em><b>Ejb Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__EJB_LINK = 12;

    /**
     * The feature id for the '<em><b>Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE__TARGET_NAME = 13;

    /**
     * The number of structural features of the the '<em>Ejb Ref Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_REF_TYPE_FEATURE_COUNT = 14;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.GbeanLocatorTypeImpl <em>Gbean Locator Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.GbeanLocatorTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getGbeanLocatorType()
     * @generated
     */
    int GBEAN_LOCATOR_TYPE = 4;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_LOCATOR_TYPE__DOMAIN = 0;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_LOCATOR_TYPE__SERVER = 1;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_LOCATOR_TYPE__APPLICATION = 2;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_LOCATOR_TYPE__MODULE = 3;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_LOCATOR_TYPE__TYPE = 4;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_LOCATOR_TYPE__NAME = 5;

    /**
     * The feature id for the '<em><b>Gbean Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_LOCATOR_TYPE__GBEAN_LINK = 6;

    /**
     * The feature id for the '<em><b>Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_LOCATOR_TYPE__TARGET_NAME = 7;

    /**
     * The number of structural features of the the '<em>Gbean Locator Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_LOCATOR_TYPE_FEATURE_COUNT = 8;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl <em>Gbean Ref Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.GbeanRefTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getGbeanRefType()
     * @generated
     */
    int GBEAN_REF_TYPE = 5;

    /**
     * The feature id for the '<em><b>Ref Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__REF_NAME = 0;

    /**
     * The feature id for the '<em><b>Ref Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__REF_TYPE = 1;

    /**
     * The feature id for the '<em><b>Proxy Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__PROXY_TYPE = 2;

    /**
     * The feature id for the '<em><b>Group</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__GROUP = 3;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__DOMAIN = 4;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__SERVER = 5;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__APPLICATION = 6;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__MODULE = 7;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__TYPE = 8;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__NAME = 9;

    /**
     * The feature id for the '<em><b>Target Name</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE__TARGET_NAME = 10;

    /**
     * The number of structural features of the the '<em>Gbean Ref Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GBEAN_REF_TYPE_FEATURE_COUNT = 11;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.PortTypeImpl <em>Port Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.PortTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getPortType()
     * @generated
     */
    int PORT_TYPE = 7;

    /**
     * The feature id for the '<em><b>Port Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_TYPE__PORT_NAME = 0;

    /**
     * The feature id for the '<em><b>Protocol</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_TYPE__PROTOCOL = 1;

    /**
     * The feature id for the '<em><b>Host</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_TYPE__HOST = 2;

    /**
     * The feature id for the '<em><b>Port</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_TYPE__PORT = 3;

    /**
     * The feature id for the '<em><b>Uri</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_TYPE__URI = 4;

    /**
     * The feature id for the '<em><b>Credentials Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_TYPE__CREDENTIALS_NAME = 5;

    /**
     * The number of structural features of the the '<em>Port Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_TYPE_FEATURE_COUNT = 6;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.PortCompletionTypeImpl <em>Port Completion Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.PortCompletionTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getPortCompletionType()
     * @generated
     */
    int PORT_COMPLETION_TYPE = 6;

    /**
     * The feature id for the '<em><b>Port Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_COMPLETION_TYPE__PORT_NAME = PORT_TYPE__PORT_NAME;

    /**
     * The feature id for the '<em><b>Protocol</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_COMPLETION_TYPE__PROTOCOL = PORT_TYPE__PROTOCOL;

    /**
     * The feature id for the '<em><b>Host</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_COMPLETION_TYPE__HOST = PORT_TYPE__HOST;

    /**
     * The feature id for the '<em><b>Port</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_COMPLETION_TYPE__PORT = PORT_TYPE__PORT;

    /**
     * The feature id for the '<em><b>Uri</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_COMPLETION_TYPE__URI = PORT_TYPE__URI;

    /**
     * The feature id for the '<em><b>Credentials Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_COMPLETION_TYPE__CREDENTIALS_NAME = PORT_TYPE__CREDENTIALS_NAME;

    /**
     * The feature id for the '<em><b>Binding Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_COMPLETION_TYPE__BINDING_NAME = PORT_TYPE_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the the '<em>Port Completion Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PORT_COMPLETION_TYPE_FEATURE_COUNT = PORT_TYPE_FEATURE_COUNT + 1;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.ResourceEnvRefTypeImpl <em>Resource Env Ref Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.ResourceEnvRefTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getResourceEnvRefType()
     * @generated
     */
    int RESOURCE_ENV_REF_TYPE = 8;

    /**
     * The feature id for the '<em><b>Ref Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE__REF_NAME = 0;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE__DOMAIN = 1;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE__SERVER = 2;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE__APPLICATION = 3;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE__MODULE = 4;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE__TYPE = 5;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE__NAME = 6;

    /**
     * The feature id for the '<em><b>Message Destination Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE__MESSAGE_DESTINATION_LINK = 7;

    /**
     * The feature id for the '<em><b>Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE__TARGET_NAME = 8;

    /**
     * The number of structural features of the the '<em>Resource Env Ref Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_ENV_REF_TYPE_FEATURE_COUNT = 9;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.ResourceLocatorTypeImpl <em>Resource Locator Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.ResourceLocatorTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getResourceLocatorType()
     * @generated
     */
    int RESOURCE_LOCATOR_TYPE = 9;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE__DOMAIN = 0;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE__SERVER = 1;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE__APPLICATION = 2;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE__MODULE = 3;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE__TYPE = 4;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE__NAME = 5;

    /**
     * The feature id for the '<em><b>Resource Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE__RESOURCE_LINK = 6;

    /**
     * The feature id for the '<em><b>Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE__TARGET_NAME = 7;

    /**
     * The feature id for the '<em><b>Url</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE__URL = 8;

    /**
     * The number of structural features of the the '<em>Resource Locator Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_LOCATOR_TYPE_FEATURE_COUNT = 9;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.ResourceRefTypeImpl <em>Resource Ref Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.ResourceRefTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getResourceRefType()
     * @generated
     */
    int RESOURCE_REF_TYPE = 10;

    /**
     * The feature id for the '<em><b>Ref Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__REF_NAME = 0;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__DOMAIN = 1;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__SERVER = 2;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__APPLICATION = 3;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__MODULE = 4;

    /**
     * The feature id for the '<em><b>Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__TYPE = 5;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__NAME = 6;

    /**
     * The feature id for the '<em><b>Resource Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__RESOURCE_LINK = 7;

    /**
     * The feature id for the '<em><b>Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__TARGET_NAME = 8;

    /**
     * The feature id for the '<em><b>Url</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE__URL = 9;

    /**
     * The number of structural features of the the '<em>Resource Ref Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RESOURCE_REF_TYPE_FEATURE_COUNT = 10;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.ServiceCompletionTypeImpl <em>Service Completion Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.ServiceCompletionTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getServiceCompletionType()
     * @generated
     */
    int SERVICE_COMPLETION_TYPE = 11;

    /**
     * The feature id for the '<em><b>Service Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SERVICE_COMPLETION_TYPE__SERVICE_NAME = 0;

    /**
     * The feature id for the '<em><b>Port</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SERVICE_COMPLETION_TYPE__PORT = 1;

    /**
     * The number of structural features of the the '<em>Service Completion Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SERVICE_COMPLETION_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.naming.impl.ServiceRefTypeImpl <em>Service Ref Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.naming.impl.ServiceRefTypeImpl
     * @see org.apache.geronimo.xml.ns.naming.impl.NamingPackageImpl#getServiceRefType()
     * @generated
     */
    int SERVICE_REF_TYPE = 12;

    /**
     * The feature id for the '<em><b>Service Ref Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SERVICE_REF_TYPE__SERVICE_REF_NAME = 0;

    /**
     * The feature id for the '<em><b>Service Completion</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SERVICE_REF_TYPE__SERVICE_COMPLETION = 1;

    /**
     * The feature id for the '<em><b>Port</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SERVICE_REF_TYPE__PORT = 2;

    /**
     * The number of structural features of the the '<em>Service Ref Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SERVICE_REF_TYPE_FEATURE_COUNT = 3;


    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.CssType <em>Css Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Css Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.CssType
     * @generated
     */
    EClass getCssType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.CssType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.naming.CssType#getDomain()
     * @see #getCssType()
     * @generated
     */
    EAttribute getCssType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.CssType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.naming.CssType#getServer()
     * @see #getCssType()
     * @generated
     */
    EAttribute getCssType_Server();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.CssType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.naming.CssType#getApplication()
     * @see #getCssType()
     * @generated
     */
    EAttribute getCssType_Application();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.CssType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.naming.CssType#getModule()
     * @see #getCssType()
     * @generated
     */
    EAttribute getCssType_Module();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.CssType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.CssType#getType()
     * @see #getCssType()
     * @generated
     */
    EAttribute getCssType_Type();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.CssType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.CssType#getName()
     * @see #getCssType()
     * @generated
     */
    EAttribute getCssType_Name();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Document Root</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot
     * @generated
     */
    EClass getDocumentRoot();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getMixed <em>Mixed</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Mixed</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getMixed()
     * @see #getDocumentRoot()
     * @generated
     */
    EAttribute getDocumentRoot_Mixed();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getXMLNSPrefixMap()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XMLNSPrefixMap();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XSI Schema Location</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getXSISchemaLocation()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XSISchemaLocation();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getCmpConnectionFactory <em>Cmp Connection Factory</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Cmp Connection Factory</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getCmpConnectionFactory()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_CmpConnectionFactory();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getEjbLocalRef <em>Ejb Local Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Ejb Local Ref</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getEjbLocalRef()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_EjbLocalRef();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getEjbRef <em>Ejb Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Ejb Ref</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getEjbRef()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_EjbRef();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceAdapter <em>Resource Adapter</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Resource Adapter</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceAdapter()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_ResourceAdapter();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceEnvRef <em>Resource Env Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Resource Env Ref</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceEnvRef()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_ResourceEnvRef();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceRef <em>Resource Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Resource Ref</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getResourceRef()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_ResourceRef();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getServiceRef <em>Service Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Service Ref</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getServiceRef()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_ServiceRef();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.DocumentRoot#getWorkmanager <em>Workmanager</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Workmanager</em>'.
     * @see org.apache.geronimo.xml.ns.naming.DocumentRoot#getWorkmanager()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_Workmanager();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType <em>Ejb Local Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Ejb Local Ref Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType
     * @generated
     */
    EClass getEjbLocalRefType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getRefName <em>Ref Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ref Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getRefName()
     * @see #getEjbLocalRefType()
     * @generated
     */
    EAttribute getEjbLocalRefType_RefName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getDomain()
     * @see #getEjbLocalRefType()
     * @generated
     */
    EAttribute getEjbLocalRefType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getServer()
     * @see #getEjbLocalRefType()
     * @generated
     */
    EAttribute getEjbLocalRefType_Server();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getApplication()
     * @see #getEjbLocalRefType()
     * @generated
     */
    EAttribute getEjbLocalRefType_Application();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getModule()
     * @see #getEjbLocalRefType()
     * @generated
     */
    EAttribute getEjbLocalRefType_Module();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getType()
     * @see #getEjbLocalRefType()
     * @generated
     */
    EAttribute getEjbLocalRefType_Type();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getName()
     * @see #getEjbLocalRefType()
     * @generated
     */
    EAttribute getEjbLocalRefType_Name();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getEjbLink <em>Ejb Link</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ejb Link</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getEjbLink()
     * @see #getEjbLocalRefType()
     * @generated
     */
    EAttribute getEjbLocalRefType_EjbLink();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getTargetName <em>Target Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Target Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbLocalRefType#getTargetName()
     * @see #getEjbLocalRefType()
     * @generated
     */
    EAttribute getEjbLocalRefType_TargetName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.EjbRefType <em>Ejb Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Ejb Ref Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType
     * @generated
     */
    EClass getEjbRefType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getRefName <em>Ref Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ref Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getRefName()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_RefName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getDomain()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getServer()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_Server();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getApplication()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_Application();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getModule()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_Module();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getType()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_Type();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getName()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_Name();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getNsCorbaloc <em>Ns Corbaloc</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ns Corbaloc</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getNsCorbaloc()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_NsCorbaloc();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getName1 <em>Name1</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name1</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getName1()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_Name1();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getCss <em>Css</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Css</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getCss()
     * @see #getEjbRefType()
     * @generated
     */
    EReference getEjbRefType_Css();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getCssLink <em>Css Link</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Css Link</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getCssLink()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_CssLink();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getCssName <em>Css Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Css Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getCssName()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_CssName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getEjbLink <em>Ejb Link</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ejb Link</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getEjbLink()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_EjbLink();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.EjbRefType#getTargetName <em>Target Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Target Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.EjbRefType#getTargetName()
     * @see #getEjbRefType()
     * @generated
     */
    EAttribute getEjbRefType_TargetName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType <em>Gbean Locator Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Gbean Locator Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType
     * @generated
     */
    EClass getGbeanLocatorType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getDomain()
     * @see #getGbeanLocatorType()
     * @generated
     */
    EAttribute getGbeanLocatorType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getServer()
     * @see #getGbeanLocatorType()
     * @generated
     */
    EAttribute getGbeanLocatorType_Server();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getApplication()
     * @see #getGbeanLocatorType()
     * @generated
     */
    EAttribute getGbeanLocatorType_Application();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getModule()
     * @see #getGbeanLocatorType()
     * @generated
     */
    EAttribute getGbeanLocatorType_Module();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getType()
     * @see #getGbeanLocatorType()
     * @generated
     */
    EAttribute getGbeanLocatorType_Type();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getName()
     * @see #getGbeanLocatorType()
     * @generated
     */
    EAttribute getGbeanLocatorType_Name();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getGbeanLink <em>Gbean Link</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Gbean Link</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getGbeanLink()
     * @see #getGbeanLocatorType()
     * @generated
     */
    EAttribute getGbeanLocatorType_GbeanLink();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getTargetName <em>Target Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Target Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanLocatorType#getTargetName()
     * @see #getGbeanLocatorType()
     * @generated
     */
    EAttribute getGbeanLocatorType_TargetName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType <em>Gbean Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Gbean Ref Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType
     * @generated
     */
    EClass getGbeanRefType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getRefName <em>Ref Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ref Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getRefName()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_RefName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getRefType <em>Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ref Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getRefType()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_RefType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getProxyType <em>Proxy Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Proxy Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getProxyType()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_ProxyType();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getGroup <em>Group</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Group</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getGroup()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_Group();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getDomain()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_Domain();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getServer()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_Server();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getApplication()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_Application();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getModule()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_Module();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getType()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_Type();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getName()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_Name();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.naming.GbeanRefType#getTargetName <em>Target Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Target Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.GbeanRefType#getTargetName()
     * @see #getGbeanRefType()
     * @generated
     */
    EAttribute getGbeanRefType_TargetName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.PortCompletionType <em>Port Completion Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Port Completion Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.PortCompletionType
     * @generated
     */
    EClass getPortCompletionType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.PortCompletionType#getBindingName <em>Binding Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Binding Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.PortCompletionType#getBindingName()
     * @see #getPortCompletionType()
     * @generated
     */
    EAttribute getPortCompletionType_BindingName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.PortType <em>Port Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Port Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.PortType
     * @generated
     */
    EClass getPortType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.PortType#getPortName <em>Port Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Port Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.PortType#getPortName()
     * @see #getPortType()
     * @generated
     */
    EAttribute getPortType_PortName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.PortType#getProtocol <em>Protocol</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Protocol</em>'.
     * @see org.apache.geronimo.xml.ns.naming.PortType#getProtocol()
     * @see #getPortType()
     * @generated
     */
    EAttribute getPortType_Protocol();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.PortType#getHost <em>Host</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Host</em>'.
     * @see org.apache.geronimo.xml.ns.naming.PortType#getHost()
     * @see #getPortType()
     * @generated
     */
    EAttribute getPortType_Host();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.PortType#getPort <em>Port</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Port</em>'.
     * @see org.apache.geronimo.xml.ns.naming.PortType#getPort()
     * @see #getPortType()
     * @generated
     */
    EAttribute getPortType_Port();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.PortType#getUri <em>Uri</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Uri</em>'.
     * @see org.apache.geronimo.xml.ns.naming.PortType#getUri()
     * @see #getPortType()
     * @generated
     */
    EAttribute getPortType_Uri();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.PortType#getCredentialsName <em>Credentials Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Credentials Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.PortType#getCredentialsName()
     * @see #getPortType()
     * @generated
     */
    EAttribute getPortType_CredentialsName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType <em>Resource Env Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Resource Env Ref Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType
     * @generated
     */
    EClass getResourceEnvRefType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getRefName <em>Ref Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ref Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getRefName()
     * @see #getResourceEnvRefType()
     * @generated
     */
    EAttribute getResourceEnvRefType_RefName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getDomain()
     * @see #getResourceEnvRefType()
     * @generated
     */
    EAttribute getResourceEnvRefType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getServer()
     * @see #getResourceEnvRefType()
     * @generated
     */
    EAttribute getResourceEnvRefType_Server();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getApplication()
     * @see #getResourceEnvRefType()
     * @generated
     */
    EAttribute getResourceEnvRefType_Application();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getModule()
     * @see #getResourceEnvRefType()
     * @generated
     */
    EAttribute getResourceEnvRefType_Module();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getType()
     * @see #getResourceEnvRefType()
     * @generated
     */
    EAttribute getResourceEnvRefType_Type();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getName()
     * @see #getResourceEnvRefType()
     * @generated
     */
    EAttribute getResourceEnvRefType_Name();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getMessageDestinationLink <em>Message Destination Link</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Message Destination Link</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getMessageDestinationLink()
     * @see #getResourceEnvRefType()
     * @generated
     */
    EAttribute getResourceEnvRefType_MessageDestinationLink();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getTargetName <em>Target Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Target Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceEnvRefType#getTargetName()
     * @see #getResourceEnvRefType()
     * @generated
     */
    EAttribute getResourceEnvRefType_TargetName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType <em>Resource Locator Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Resource Locator Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType
     * @generated
     */
    EClass getResourceLocatorType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getDomain()
     * @see #getResourceLocatorType()
     * @generated
     */
    EAttribute getResourceLocatorType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getServer()
     * @see #getResourceLocatorType()
     * @generated
     */
    EAttribute getResourceLocatorType_Server();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getApplication()
     * @see #getResourceLocatorType()
     * @generated
     */
    EAttribute getResourceLocatorType_Application();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getModule()
     * @see #getResourceLocatorType()
     * @generated
     */
    EAttribute getResourceLocatorType_Module();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getType()
     * @see #getResourceLocatorType()
     * @generated
     */
    EAttribute getResourceLocatorType_Type();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getName()
     * @see #getResourceLocatorType()
     * @generated
     */
    EAttribute getResourceLocatorType_Name();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getResourceLink <em>Resource Link</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Resource Link</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getResourceLink()
     * @see #getResourceLocatorType()
     * @generated
     */
    EAttribute getResourceLocatorType_ResourceLink();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getTargetName <em>Target Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Target Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getTargetName()
     * @see #getResourceLocatorType()
     * @generated
     */
    EAttribute getResourceLocatorType_TargetName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getUrl <em>Url</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Url</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceLocatorType#getUrl()
     * @see #getResourceLocatorType()
     * @generated
     */
    EAttribute getResourceLocatorType_Url();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType <em>Resource Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Resource Ref Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType
     * @generated
     */
    EClass getResourceRefType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getRefName <em>Ref Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ref Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getRefName()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_RefName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getDomain()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getServer()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_Server();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getApplication()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_Application();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getModule()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_Module();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getType()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_Type();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getName()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_Name();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getResourceLink <em>Resource Link</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Resource Link</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getResourceLink()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_ResourceLink();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getTargetName <em>Target Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Target Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getTargetName()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_TargetName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ResourceRefType#getUrl <em>Url</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Url</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ResourceRefType#getUrl()
     * @see #getResourceRefType()
     * @generated
     */
    EAttribute getResourceRefType_Url();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.ServiceCompletionType <em>Service Completion Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Service Completion Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ServiceCompletionType
     * @generated
     */
    EClass getServiceCompletionType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ServiceCompletionType#getServiceName <em>Service Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Service Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ServiceCompletionType#getServiceName()
     * @see #getServiceCompletionType()
     * @generated
     */
    EAttribute getServiceCompletionType_ServiceName();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.naming.ServiceCompletionType#getPort <em>Port</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Port</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ServiceCompletionType#getPort()
     * @see #getServiceCompletionType()
     * @generated
     */
    EReference getServiceCompletionType_Port();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.naming.ServiceRefType <em>Service Ref Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Service Ref Type</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ServiceRefType
     * @generated
     */
    EClass getServiceRefType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.naming.ServiceRefType#getServiceRefName <em>Service Ref Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Service Ref Name</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ServiceRefType#getServiceRefName()
     * @see #getServiceRefType()
     * @generated
     */
    EAttribute getServiceRefType_ServiceRefName();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.naming.ServiceRefType#getServiceCompletion <em>Service Completion</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Service Completion</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ServiceRefType#getServiceCompletion()
     * @see #getServiceRefType()
     * @generated
     */
    EReference getServiceRefType_ServiceCompletion();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.naming.ServiceRefType#getPort <em>Port</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Port</em>'.
     * @see org.apache.geronimo.xml.ns.naming.ServiceRefType#getPort()
     * @see #getServiceRefType()
     * @generated
     */
    EReference getServiceRefType_Port();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    NamingFactory getNamingFactory();

} //NamingPackage
