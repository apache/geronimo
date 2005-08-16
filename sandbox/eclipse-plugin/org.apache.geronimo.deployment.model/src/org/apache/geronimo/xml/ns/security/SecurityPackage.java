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
package org.apache.geronimo.xml.ns.security;

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
 * @see org.apache.geronimo.xml.ns.security.SecurityFactory
 * @model kind="package"
 * @generated
 */
public interface SecurityPackage extends EPackage {
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
    String eNAME = "security";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "http://geronimo.apache.org/xml/ns/security";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "security";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    SecurityPackage eINSTANCE = org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl.init();

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.DefaultPrincipalTypeImpl <em>Default Principal Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.DefaultPrincipalTypeImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getDefaultPrincipalType()
     * @generated
     */
    int DEFAULT_PRINCIPAL_TYPE = 0;

    /**
     * The feature id for the '<em><b>Description</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEFAULT_PRINCIPAL_TYPE__DESCRIPTION = 0;

    /**
     * The feature id for the '<em><b>Principal</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEFAULT_PRINCIPAL_TYPE__PRINCIPAL = 1;

    /**
     * The feature id for the '<em><b>Named Username Password Credential</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEFAULT_PRINCIPAL_TYPE__NAMED_USERNAME_PASSWORD_CREDENTIAL = 2;

    /**
     * The feature id for the '<em><b>Realm Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEFAULT_PRINCIPAL_TYPE__REALM_NAME = 3;

    /**
     * The number of structural features of the the '<em>Default Principal Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DEFAULT_PRINCIPAL_TYPE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.DescriptionTypeImpl <em>Description Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.DescriptionTypeImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getDescriptionType()
     * @generated
     */
    int DESCRIPTION_TYPE = 1;

    /**
     * The feature id for the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DESCRIPTION_TYPE__VALUE = 0;

    /**
     * The feature id for the '<em><b>Lang</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DESCRIPTION_TYPE__LANG = 1;

    /**
     * The number of structural features of the the '<em>Description Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DESCRIPTION_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.DistinguishedNameTypeImpl <em>Distinguished Name Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.DistinguishedNameTypeImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getDistinguishedNameType()
     * @generated
     */
    int DISTINGUISHED_NAME_TYPE = 2;

    /**
     * The feature id for the '<em><b>Description</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DISTINGUISHED_NAME_TYPE__DESCRIPTION = 0;

    /**
     * The feature id for the '<em><b>Designated Run As</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DISTINGUISHED_NAME_TYPE__DESIGNATED_RUN_AS = 1;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DISTINGUISHED_NAME_TYPE__NAME = 2;

    /**
     * The number of structural features of the the '<em>Distinguished Name Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DISTINGUISHED_NAME_TYPE_FEATURE_COUNT = 3;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.DocumentRootImpl <em>Document Root</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.DocumentRootImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getDocumentRoot()
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
     * The feature id for the '<em><b>Default Principal</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__DEFAULT_PRINCIPAL = 3;

    /**
     * The feature id for the '<em><b>Security</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__SECURITY = 4;

    /**
     * The number of structural features of the the '<em>Document Root</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT_FEATURE_COUNT = 5;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.NamedUsernamePasswordCredentialTypeImpl <em>Named Username Password Credential Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.NamedUsernamePasswordCredentialTypeImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getNamedUsernamePasswordCredentialType()
     * @generated
     */
    int NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE = 4;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE__NAME = 0;

    /**
     * The feature id for the '<em><b>Username</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE__USERNAME = 1;

    /**
     * The feature id for the '<em><b>Password</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE__PASSWORD = 2;

    /**
     * The number of structural features of the the '<em>Named Username Password Credential Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int NAMED_USERNAME_PASSWORD_CREDENTIAL_TYPE_FEATURE_COUNT = 3;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.PrincipalTypeImpl <em>Principal Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.PrincipalTypeImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getPrincipalType()
     * @generated
     */
    int PRINCIPAL_TYPE = 5;

    /**
     * The feature id for the '<em><b>Description</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PRINCIPAL_TYPE__DESCRIPTION = 0;

    /**
     * The feature id for the '<em><b>Class</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PRINCIPAL_TYPE__CLASS = 1;

    /**
     * The feature id for the '<em><b>Designated Run As</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PRINCIPAL_TYPE__DESIGNATED_RUN_AS = 2;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PRINCIPAL_TYPE__NAME = 3;

    /**
     * The number of structural features of the the '<em>Principal Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PRINCIPAL_TYPE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.RealmTypeImpl <em>Realm Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.RealmTypeImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getRealmType()
     * @generated
     */
    int REALM_TYPE = 6;

    /**
     * The feature id for the '<em><b>Description</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REALM_TYPE__DESCRIPTION = 0;

    /**
     * The feature id for the '<em><b>Principal</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REALM_TYPE__PRINCIPAL = 1;

    /**
     * The feature id for the '<em><b>Realm Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REALM_TYPE__REALM_NAME = 2;

    /**
     * The number of structural features of the the '<em>Realm Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int REALM_TYPE_FEATURE_COUNT = 3;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.RoleMappingsTypeImpl <em>Role Mappings Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.RoleMappingsTypeImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getRoleMappingsType()
     * @generated
     */
    int ROLE_MAPPINGS_TYPE = 7;

    /**
     * The feature id for the '<em><b>Role</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ROLE_MAPPINGS_TYPE__ROLE = 0;

    /**
     * The number of structural features of the the '<em>Role Mappings Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ROLE_MAPPINGS_TYPE_FEATURE_COUNT = 1;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.RoleTypeImpl <em>Role Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.RoleTypeImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getRoleType()
     * @generated
     */
    int ROLE_TYPE = 8;

    /**
     * The feature id for the '<em><b>Description</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ROLE_TYPE__DESCRIPTION = 0;

    /**
     * The feature id for the '<em><b>Realm</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ROLE_TYPE__REALM = 1;

    /**
     * The feature id for the '<em><b>Distinguished Name</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ROLE_TYPE__DISTINGUISHED_NAME = 2;

    /**
     * The feature id for the '<em><b>Role Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ROLE_TYPE__ROLE_NAME = 3;

    /**
     * The number of structural features of the the '<em>Role Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ROLE_TYPE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.apache.geronimo.xml.ns.security.impl.SecurityTypeImpl <em>Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityTypeImpl
     * @see org.apache.geronimo.xml.ns.security.impl.SecurityPackageImpl#getSecurityType()
     * @generated
     */
    int SECURITY_TYPE = 9;

    /**
     * The feature id for the '<em><b>Description</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SECURITY_TYPE__DESCRIPTION = 0;

    /**
     * The feature id for the '<em><b>Default Principal</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SECURITY_TYPE__DEFAULT_PRINCIPAL = 1;

    /**
     * The feature id for the '<em><b>Role Mappings</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SECURITY_TYPE__ROLE_MAPPINGS = 2;

    /**
     * The feature id for the '<em><b>Default Role</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SECURITY_TYPE__DEFAULT_ROLE = 3;

    /**
     * The feature id for the '<em><b>Doas Current Caller</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SECURITY_TYPE__DOAS_CURRENT_CALLER = 4;

    /**
     * The feature id for the '<em><b>Use Context Handler</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SECURITY_TYPE__USE_CONTEXT_HANDLER = 5;

    /**
     * The number of structural features of the the '<em>Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SECURITY_TYPE_FEATURE_COUNT = 6;


    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType <em>Default Principal Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Default Principal Type</em>'.
     * @see org.apache.geronimo.xml.ns.security.DefaultPrincipalType
     * @generated
     */
    EClass getDefaultPrincipalType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getDescription <em>Description</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Description</em>'.
     * @see org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getDescription()
     * @see #getDefaultPrincipalType()
     * @generated
     */
    EReference getDefaultPrincipalType_Description();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getPrincipal <em>Principal</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Principal</em>'.
     * @see org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getPrincipal()
     * @see #getDefaultPrincipalType()
     * @generated
     */
    EReference getDefaultPrincipalType_Principal();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getNamedUsernamePasswordCredential <em>Named Username Password Credential</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Named Username Password Credential</em>'.
     * @see org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getNamedUsernamePasswordCredential()
     * @see #getDefaultPrincipalType()
     * @generated
     */
    EReference getDefaultPrincipalType_NamedUsernamePasswordCredential();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getRealmName <em>Realm Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Realm Name</em>'.
     * @see org.apache.geronimo.xml.ns.security.DefaultPrincipalType#getRealmName()
     * @see #getDefaultPrincipalType()
     * @generated
     */
    EAttribute getDefaultPrincipalType_RealmName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.DescriptionType <em>Description Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Description Type</em>'.
     * @see org.apache.geronimo.xml.ns.security.DescriptionType
     * @generated
     */
    EClass getDescriptionType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.DescriptionType#getValue <em>Value</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Value</em>'.
     * @see org.apache.geronimo.xml.ns.security.DescriptionType#getValue()
     * @see #getDescriptionType()
     * @generated
     */
    EAttribute getDescriptionType_Value();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.DescriptionType#getLang <em>Lang</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Lang</em>'.
     * @see org.apache.geronimo.xml.ns.security.DescriptionType#getLang()
     * @see #getDescriptionType()
     * @generated
     */
    EAttribute getDescriptionType_Lang();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType <em>Distinguished Name Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Distinguished Name Type</em>'.
     * @see org.apache.geronimo.xml.ns.security.DistinguishedNameType
     * @generated
     */
    EClass getDistinguishedNameType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#getDescription <em>Description</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Description</em>'.
     * @see org.apache.geronimo.xml.ns.security.DistinguishedNameType#getDescription()
     * @see #getDistinguishedNameType()
     * @generated
     */
    EReference getDistinguishedNameType_Description();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#isDesignatedRunAs <em>Designated Run As</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Designated Run As</em>'.
     * @see org.apache.geronimo.xml.ns.security.DistinguishedNameType#isDesignatedRunAs()
     * @see #getDistinguishedNameType()
     * @generated
     */
    EAttribute getDistinguishedNameType_DesignatedRunAs();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.security.DistinguishedNameType#getName()
     * @see #getDistinguishedNameType()
     * @generated
     */
    EAttribute getDistinguishedNameType_Name();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Document Root</em>'.
     * @see org.apache.geronimo.xml.ns.security.DocumentRoot
     * @generated
     */
    EClass getDocumentRoot();

    /**
     * Returns the meta object for the attribute list '{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getMixed <em>Mixed</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Mixed</em>'.
     * @see org.apache.geronimo.xml.ns.security.DocumentRoot#getMixed()
     * @see #getDocumentRoot()
     * @generated
     */
    EAttribute getDocumentRoot_Mixed();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
     * @see org.apache.geronimo.xml.ns.security.DocumentRoot#getXMLNSPrefixMap()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XMLNSPrefixMap();

    /**
     * Returns the meta object for the map '{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XSI Schema Location</em>'.
     * @see org.apache.geronimo.xml.ns.security.DocumentRoot#getXSISchemaLocation()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XSISchemaLocation();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getDefaultPrincipal <em>Default Principal</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Default Principal</em>'.
     * @see org.apache.geronimo.xml.ns.security.DocumentRoot#getDefaultPrincipal()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_DefaultPrincipal();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.security.DocumentRoot#getSecurity <em>Security</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Security</em>'.
     * @see org.apache.geronimo.xml.ns.security.DocumentRoot#getSecurity()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_Security();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType <em>Named Username Password Credential Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Named Username Password Credential Type</em>'.
     * @see org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType
     * @generated
     */
    EClass getNamedUsernamePasswordCredentialType();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType#getName()
     * @see #getNamedUsernamePasswordCredentialType()
     * @generated
     */
    EAttribute getNamedUsernamePasswordCredentialType_Name();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType#getUsername <em>Username</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Username</em>'.
     * @see org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType#getUsername()
     * @see #getNamedUsernamePasswordCredentialType()
     * @generated
     */
    EAttribute getNamedUsernamePasswordCredentialType_Username();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType#getPassword <em>Password</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Password</em>'.
     * @see org.apache.geronimo.xml.ns.security.NamedUsernamePasswordCredentialType#getPassword()
     * @see #getNamedUsernamePasswordCredentialType()
     * @generated
     */
    EAttribute getNamedUsernamePasswordCredentialType_Password();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.PrincipalType <em>Principal Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Principal Type</em>'.
     * @see org.apache.geronimo.xml.ns.security.PrincipalType
     * @generated
     */
    EClass getPrincipalType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.PrincipalType#getDescription <em>Description</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Description</em>'.
     * @see org.apache.geronimo.xml.ns.security.PrincipalType#getDescription()
     * @see #getPrincipalType()
     * @generated
     */
    EReference getPrincipalType_Description();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.PrincipalType#getClass_ <em>Class</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Class</em>'.
     * @see org.apache.geronimo.xml.ns.security.PrincipalType#getClass_()
     * @see #getPrincipalType()
     * @generated
     */
    EAttribute getPrincipalType_Class();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.PrincipalType#isDesignatedRunAs <em>Designated Run As</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Designated Run As</em>'.
     * @see org.apache.geronimo.xml.ns.security.PrincipalType#isDesignatedRunAs()
     * @see #getPrincipalType()
     * @generated
     */
    EAttribute getPrincipalType_DesignatedRunAs();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.PrincipalType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.apache.geronimo.xml.ns.security.PrincipalType#getName()
     * @see #getPrincipalType()
     * @generated
     */
    EAttribute getPrincipalType_Name();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.RealmType <em>Realm Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Realm Type</em>'.
     * @see org.apache.geronimo.xml.ns.security.RealmType
     * @generated
     */
    EClass getRealmType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.RealmType#getDescription <em>Description</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Description</em>'.
     * @see org.apache.geronimo.xml.ns.security.RealmType#getDescription()
     * @see #getRealmType()
     * @generated
     */
    EReference getRealmType_Description();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.RealmType#getPrincipal <em>Principal</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Principal</em>'.
     * @see org.apache.geronimo.xml.ns.security.RealmType#getPrincipal()
     * @see #getRealmType()
     * @generated
     */
    EReference getRealmType_Principal();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.RealmType#getRealmName <em>Realm Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Realm Name</em>'.
     * @see org.apache.geronimo.xml.ns.security.RealmType#getRealmName()
     * @see #getRealmType()
     * @generated
     */
    EAttribute getRealmType_RealmName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.RoleMappingsType <em>Role Mappings Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Role Mappings Type</em>'.
     * @see org.apache.geronimo.xml.ns.security.RoleMappingsType
     * @generated
     */
    EClass getRoleMappingsType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.RoleMappingsType#getRole <em>Role</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Role</em>'.
     * @see org.apache.geronimo.xml.ns.security.RoleMappingsType#getRole()
     * @see #getRoleMappingsType()
     * @generated
     */
    EReference getRoleMappingsType_Role();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.RoleType <em>Role Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Role Type</em>'.
     * @see org.apache.geronimo.xml.ns.security.RoleType
     * @generated
     */
    EClass getRoleType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.RoleType#getDescription <em>Description</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Description</em>'.
     * @see org.apache.geronimo.xml.ns.security.RoleType#getDescription()
     * @see #getRoleType()
     * @generated
     */
    EReference getRoleType_Description();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.RoleType#getRealm <em>Realm</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Realm</em>'.
     * @see org.apache.geronimo.xml.ns.security.RoleType#getRealm()
     * @see #getRoleType()
     * @generated
     */
    EReference getRoleType_Realm();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.RoleType#getDistinguishedName <em>Distinguished Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Distinguished Name</em>'.
     * @see org.apache.geronimo.xml.ns.security.RoleType#getDistinguishedName()
     * @see #getRoleType()
     * @generated
     */
    EReference getRoleType_DistinguishedName();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.RoleType#getRoleName <em>Role Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Role Name</em>'.
     * @see org.apache.geronimo.xml.ns.security.RoleType#getRoleName()
     * @see #getRoleType()
     * @generated
     */
    EAttribute getRoleType_RoleName();

    /**
     * Returns the meta object for class '{@link org.apache.geronimo.xml.ns.security.SecurityType <em>Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Type</em>'.
     * @see org.apache.geronimo.xml.ns.security.SecurityType
     * @generated
     */
    EClass getSecurityType();

    /**
     * Returns the meta object for the containment reference list '{@link org.apache.geronimo.xml.ns.security.SecurityType#getDescription <em>Description</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Description</em>'.
     * @see org.apache.geronimo.xml.ns.security.SecurityType#getDescription()
     * @see #getSecurityType()
     * @generated
     */
    EReference getSecurityType_Description();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.security.SecurityType#getDefaultPrincipal <em>Default Principal</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Default Principal</em>'.
     * @see org.apache.geronimo.xml.ns.security.SecurityType#getDefaultPrincipal()
     * @see #getSecurityType()
     * @generated
     */
    EReference getSecurityType_DefaultPrincipal();

    /**
     * Returns the meta object for the containment reference '{@link org.apache.geronimo.xml.ns.security.SecurityType#getRoleMappings <em>Role Mappings</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Role Mappings</em>'.
     * @see org.apache.geronimo.xml.ns.security.SecurityType#getRoleMappings()
     * @see #getSecurityType()
     * @generated
     */
    EReference getSecurityType_RoleMappings();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.SecurityType#getDefaultRole <em>Default Role</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Default Role</em>'.
     * @see org.apache.geronimo.xml.ns.security.SecurityType#getDefaultRole()
     * @see #getSecurityType()
     * @generated
     */
    EAttribute getSecurityType_DefaultRole();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.SecurityType#isDoasCurrentCaller <em>Doas Current Caller</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Doas Current Caller</em>'.
     * @see org.apache.geronimo.xml.ns.security.SecurityType#isDoasCurrentCaller()
     * @see #getSecurityType()
     * @generated
     */
    EAttribute getSecurityType_DoasCurrentCaller();

    /**
     * Returns the meta object for the attribute '{@link org.apache.geronimo.xml.ns.security.SecurityType#isUseContextHandler <em>Use Context Handler</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Use Context Handler</em>'.
     * @see org.apache.geronimo.xml.ns.security.SecurityType#isUseContextHandler()
     * @see #getSecurityType()
     * @generated
     */
    EAttribute getSecurityType_UseContextHandler();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    SecurityFactory getSecurityFactory();

} //SecurityPackage
