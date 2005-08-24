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
package org.openejb.xml.ns.openejb.jar;

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
 *             
 *             Partial schema containing common naming elements which can be included in other schemas.
 *             
 *         
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
 * @see org.openejb.xml.ns.openejb.jar.JarFactory
 * @model kind="package"
 * @generated
 */
public interface JarPackage extends EPackage {
    /**
     * The package name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNAME = "jar";

    /**
     * The package namespace URI.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_URI = "http://www.openejb.org/xml/ns/openejb-jar";

    /**
     * The package namespace name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String eNS_PREFIX = "jar";

    /**
     * The singleton instance of the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    JarPackage eINSTANCE = org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl.init();

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.ActivationConfigPropertyTypeImpl <em>Activation Config Property Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.ActivationConfigPropertyTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getActivationConfigPropertyType()
     * @generated
     */
    int ACTIVATION_CONFIG_PROPERTY_TYPE = 0;

    /**
     * The feature id for the '<em><b>Activation Config Property Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_NAME = 0;

    /**
     * The feature id for the '<em><b>Activation Config Property Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ACTIVATION_CONFIG_PROPERTY_TYPE__ACTIVATION_CONFIG_PROPERTY_VALUE = 1;

    /**
     * The number of structural features of the the '<em>Activation Config Property Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ACTIVATION_CONFIG_PROPERTY_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.ActivationConfigTypeImpl <em>Activation Config Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.ActivationConfigTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getActivationConfigType()
     * @generated
     */
    int ACTIVATION_CONFIG_TYPE = 1;

    /**
     * The feature id for the '<em><b>Description</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ACTIVATION_CONFIG_TYPE__DESCRIPTION = 0;

    /**
     * The feature id for the '<em><b>Activation Config Property</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ACTIVATION_CONFIG_TYPE__ACTIVATION_CONFIG_PROPERTY = 1;

    /**
     * The number of structural features of the the '<em>Activation Config Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ACTIVATION_CONFIG_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.CmpFieldGroupMappingTypeImpl <em>Cmp Field Group Mapping Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.CmpFieldGroupMappingTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getCmpFieldGroupMappingType()
     * @generated
     */
    int CMP_FIELD_GROUP_MAPPING_TYPE = 2;

    /**
     * The feature id for the '<em><b>Group Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMP_FIELD_GROUP_MAPPING_TYPE__GROUP_NAME = 0;

    /**
     * The feature id for the '<em><b>Cmp Field Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMP_FIELD_GROUP_MAPPING_TYPE__CMP_FIELD_NAME = 1;

    /**
     * The number of structural features of the the '<em>Cmp Field Group Mapping Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMP_FIELD_GROUP_MAPPING_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.CmpFieldMappingTypeImpl <em>Cmp Field Mapping Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.CmpFieldMappingTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getCmpFieldMappingType()
     * @generated
     */
    int CMP_FIELD_MAPPING_TYPE = 3;

    /**
     * The feature id for the '<em><b>Cmp Field Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMP_FIELD_MAPPING_TYPE__CMP_FIELD_NAME = 0;

    /**
     * The feature id for the '<em><b>Cmp Field Class</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMP_FIELD_MAPPING_TYPE__CMP_FIELD_CLASS = 1;

    /**
     * The feature id for the '<em><b>Table Column</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMP_FIELD_MAPPING_TYPE__TABLE_COLUMN = 2;

    /**
     * The feature id for the '<em><b>Sql Type</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMP_FIELD_MAPPING_TYPE__SQL_TYPE = 3;

    /**
     * The feature id for the '<em><b>Type Converter</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMP_FIELD_MAPPING_TYPE__TYPE_CONVERTER = 4;

    /**
     * The number of structural features of the the '<em>Cmp Field Mapping Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMP_FIELD_MAPPING_TYPE_FEATURE_COUNT = 5;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.CmrFieldGroupMappingTypeImpl <em>Cmr Field Group Mapping Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.CmrFieldGroupMappingTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getCmrFieldGroupMappingType()
     * @generated
     */
    int CMR_FIELD_GROUP_MAPPING_TYPE = 4;

    /**
     * The feature id for the '<em><b>Group Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_GROUP_MAPPING_TYPE__GROUP_NAME = 0;

    /**
     * The feature id for the '<em><b>Cmr Field Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_GROUP_MAPPING_TYPE__CMR_FIELD_NAME = 1;

    /**
     * The number of structural features of the the '<em>Cmr Field Group Mapping Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_GROUP_MAPPING_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.CmrFieldMappingTypeImpl <em>Cmr Field Mapping Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.CmrFieldMappingTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getCmrFieldMappingType()
     * @generated
     */
    int CMR_FIELD_MAPPING_TYPE = 5;

    /**
     * The feature id for the '<em><b>Key Column</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_MAPPING_TYPE__KEY_COLUMN = 0;

    /**
     * The feature id for the '<em><b>Foreign Key Column</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_MAPPING_TYPE__FOREIGN_KEY_COLUMN = 1;

    /**
     * The number of structural features of the the '<em>Cmr Field Mapping Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_MAPPING_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.CmrFieldTypeImpl <em>Cmr Field Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.CmrFieldTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getCmrFieldType()
     * @generated
     */
    int CMR_FIELD_TYPE = 6;

    /**
     * The feature id for the '<em><b>Cmr Field Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_TYPE__CMR_FIELD_NAME = 0;

    /**
     * The number of structural features of the the '<em>Cmr Field Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_TYPE_FEATURE_COUNT = 1;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.CmrFieldType1Impl <em>Cmr Field Type1</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.CmrFieldType1Impl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getCmrFieldType1()
     * @generated
     */
    int CMR_FIELD_TYPE1 = 7;

    /**
     * The feature id for the '<em><b>Cmr Field Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_TYPE1__CMR_FIELD_NAME = 0;

    /**
     * The feature id for the '<em><b>Group Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_TYPE1__GROUP_NAME = 1;

    /**
     * The number of structural features of the the '<em>Cmr Field Type1</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int CMR_FIELD_TYPE1_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.DocumentRootImpl <em>Document Root</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.DocumentRootImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getDocumentRoot()
     * @generated
     */
    int DOCUMENT_ROOT = 8;

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
     * The feature id for the '<em><b>Openejb Jar</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT__OPENEJB_JAR = 3;

    /**
     * The number of structural features of the the '<em>Document Root</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int DOCUMENT_ROOT_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.EjbRelationshipRoleTypeImpl <em>Ejb Relationship Role Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.EjbRelationshipRoleTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getEjbRelationshipRoleType()
     * @generated
     */
    int EJB_RELATIONSHIP_ROLE_TYPE = 9;

    /**
     * The feature id for the '<em><b>Ejb Relationship Role Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATIONSHIP_ROLE_TYPE__EJB_RELATIONSHIP_ROLE_NAME = 0;

    /**
     * The feature id for the '<em><b>Relationship Role Source</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATIONSHIP_ROLE_TYPE__RELATIONSHIP_ROLE_SOURCE = 1;

    /**
     * The feature id for the '<em><b>Cmr Field</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATIONSHIP_ROLE_TYPE__CMR_FIELD = 2;

    /**
     * The feature id for the '<em><b>Foreign Key Column On Source</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATIONSHIP_ROLE_TYPE__FOREIGN_KEY_COLUMN_ON_SOURCE = 3;

    /**
     * The feature id for the '<em><b>Role Mapping</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATIONSHIP_ROLE_TYPE__ROLE_MAPPING = 4;

    /**
     * The number of structural features of the the '<em>Ejb Relationship Role Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATIONSHIP_ROLE_TYPE_FEATURE_COUNT = 5;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.EjbRelationTypeImpl <em>Ejb Relation Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.EjbRelationTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getEjbRelationType()
     * @generated
     */
    int EJB_RELATION_TYPE = 10;

    /**
     * The feature id for the '<em><b>Ejb Relation Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATION_TYPE__EJB_RELATION_NAME = 0;

    /**
     * The feature id for the '<em><b>Many To Many Table Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATION_TYPE__MANY_TO_MANY_TABLE_NAME = 1;

    /**
     * The feature id for the '<em><b>Ejb Relationship Role</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATION_TYPE__EJB_RELATIONSHIP_ROLE = 2;

    /**
     * The number of structural features of the the '<em>Ejb Relation Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int EJB_RELATION_TYPE_FEATURE_COUNT = 3;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.EnterpriseBeansTypeImpl <em>Enterprise Beans Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.EnterpriseBeansTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getEnterpriseBeansType()
     * @generated
     */
    int ENTERPRISE_BEANS_TYPE = 11;

    /**
     * The feature id for the '<em><b>Group</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTERPRISE_BEANS_TYPE__GROUP = 0;

    /**
     * The feature id for the '<em><b>Session</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTERPRISE_BEANS_TYPE__SESSION = 1;

    /**
     * The feature id for the '<em><b>Entity</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTERPRISE_BEANS_TYPE__ENTITY = 2;

    /**
     * The feature id for the '<em><b>Message Driven</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTERPRISE_BEANS_TYPE__MESSAGE_DRIVEN = 3;

    /**
     * The number of structural features of the the '<em>Enterprise Beans Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTERPRISE_BEANS_TYPE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl <em>Entity Bean Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.EntityBeanTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getEntityBeanType()
     * @generated
     */
    int ENTITY_BEAN_TYPE = 12;

    /**
     * The feature id for the '<em><b>Ejb Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__EJB_NAME = 0;

    /**
     * The feature id for the '<em><b>Jndi Name</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__JNDI_NAME = 1;

    /**
     * The feature id for the '<em><b>Local Jndi Name</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__LOCAL_JNDI_NAME = 2;

    /**
     * The feature id for the '<em><b>Tss Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__TSS_TARGET_NAME = 3;

    /**
     * The feature id for the '<em><b>Tss Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__TSS_LINK = 4;

    /**
     * The feature id for the '<em><b>Tss</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__TSS = 5;

    /**
     * The feature id for the '<em><b>Table Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__TABLE_NAME = 6;

    /**
     * The feature id for the '<em><b>Cmp Field Mapping</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__CMP_FIELD_MAPPING = 7;

    /**
     * The feature id for the '<em><b>Primkey Field</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__PRIMKEY_FIELD = 8;

    /**
     * The feature id for the '<em><b>Key Generator</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__KEY_GENERATOR = 9;

    /**
     * The feature id for the '<em><b>Prefetch Group</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__PREFETCH_GROUP = 10;

    /**
     * The feature id for the '<em><b>Ejb Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__EJB_REF = 11;

    /**
     * The feature id for the '<em><b>Ejb Local Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__EJB_LOCAL_REF = 12;

    /**
     * The feature id for the '<em><b>Service Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__SERVICE_REF = 13;

    /**
     * The feature id for the '<em><b>Resource Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__RESOURCE_REF = 14;

    /**
     * The feature id for the '<em><b>Resource Env Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__RESOURCE_ENV_REF = 15;

    /**
     * The feature id for the '<em><b>Query</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__QUERY = 16;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE__ID = 17;

    /**
     * The number of structural features of the the '<em>Entity Bean Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_BEAN_TYPE_FEATURE_COUNT = 18;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.EntityGroupMappingTypeImpl <em>Entity Group Mapping Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.EntityGroupMappingTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getEntityGroupMappingType()
     * @generated
     */
    int ENTITY_GROUP_MAPPING_TYPE = 13;

    /**
     * The feature id for the '<em><b>Group Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_GROUP_MAPPING_TYPE__GROUP_NAME = 0;

    /**
     * The number of structural features of the the '<em>Entity Group Mapping Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ENTITY_GROUP_MAPPING_TYPE_FEATURE_COUNT = 1;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.GroupTypeImpl <em>Group Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.GroupTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getGroupType()
     * @generated
     */
    int GROUP_TYPE = 14;

    /**
     * The feature id for the '<em><b>Group Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_TYPE__GROUP_NAME = 0;

    /**
     * The feature id for the '<em><b>Cmp Field Name</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_TYPE__CMP_FIELD_NAME = 1;

    /**
     * The feature id for the '<em><b>Cmr Field</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_TYPE__CMR_FIELD = 2;

    /**
     * The number of structural features of the the '<em>Group Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int GROUP_TYPE_FEATURE_COUNT = 3;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl <em>Message Driven Bean Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.MessageDrivenBeanTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getMessageDrivenBeanType()
     * @generated
     */
    int MESSAGE_DRIVEN_BEAN_TYPE = 15;

    /**
     * The feature id for the '<em><b>Ejb Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE__EJB_NAME = 0;

    /**
     * The feature id for the '<em><b>Resource Adapter</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ADAPTER = 1;

    /**
     * The feature id for the '<em><b>Activation Config</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE__ACTIVATION_CONFIG = 2;

    /**
     * The feature id for the '<em><b>Ejb Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE__EJB_REF = 3;

    /**
     * The feature id for the '<em><b>Ejb Local Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE__EJB_LOCAL_REF = 4;

    /**
     * The feature id for the '<em><b>Service Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE__SERVICE_REF = 5;

    /**
     * The feature id for the '<em><b>Resource Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_REF = 6;

    /**
     * The feature id for the '<em><b>Resource Env Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE__RESOURCE_ENV_REF = 7;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE__ID = 8;

    /**
     * The number of structural features of the the '<em>Message Driven Bean Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int MESSAGE_DRIVEN_BEAN_TYPE_FEATURE_COUNT = 9;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.MethodParamsTypeImpl <em>Method Params Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.MethodParamsTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getMethodParamsType()
     * @generated
     */
    int METHOD_PARAMS_TYPE = 16;

    /**
     * The feature id for the '<em><b>Method Param</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int METHOD_PARAMS_TYPE__METHOD_PARAM = 0;

    /**
     * The number of structural features of the the '<em>Method Params Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int METHOD_PARAMS_TYPE_FEATURE_COUNT = 1;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl <em>Openejb Jar Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.OpenejbJarTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getOpenejbJarType()
     * @generated
     */
    int OPENEJB_JAR_TYPE = 17;

    /**
     * The feature id for the '<em><b>Dependency</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__DEPENDENCY = 0;

    /**
     * The feature id for the '<em><b>Cmp Connection Factory</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__CMP_CONNECTION_FACTORY = 1;

    /**
     * The feature id for the '<em><b>Ejb Ql Compiler Factory</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__EJB_QL_COMPILER_FACTORY = 2;

    /**
     * The feature id for the '<em><b>Db Syntax Factory</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__DB_SYNTAX_FACTORY = 3;

    /**
     * The feature id for the '<em><b>Enforce Foreign Key Constraints</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__ENFORCE_FOREIGN_KEY_CONSTRAINTS = 4;

    /**
     * The feature id for the '<em><b>Enterprise Beans</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__ENTERPRISE_BEANS = 5;

    /**
     * The feature id for the '<em><b>Relationships</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__RELATIONSHIPS = 6;

    /**
     * The feature id for the '<em><b>Security</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__SECURITY = 7;

    /**
     * The feature id for the '<em><b>Gbean</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__GBEAN = 8;

    /**
     * The feature id for the '<em><b>Config Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__CONFIG_ID = 9;

    /**
     * The feature id for the '<em><b>Parent Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE__PARENT_ID = 10;

    /**
     * The number of structural features of the the '<em>Openejb Jar Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int OPENEJB_JAR_TYPE_FEATURE_COUNT = 11;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.PrefetchGroupTypeImpl <em>Prefetch Group Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.PrefetchGroupTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getPrefetchGroupType()
     * @generated
     */
    int PREFETCH_GROUP_TYPE = 18;

    /**
     * The feature id for the '<em><b>Group</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PREFETCH_GROUP_TYPE__GROUP = 0;

    /**
     * The feature id for the '<em><b>Entity Group Mapping</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PREFETCH_GROUP_TYPE__ENTITY_GROUP_MAPPING = 1;

    /**
     * The feature id for the '<em><b>Cmp Field Group Mapping</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PREFETCH_GROUP_TYPE__CMP_FIELD_GROUP_MAPPING = 2;

    /**
     * The feature id for the '<em><b>Cmr Field Group Mapping</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PREFETCH_GROUP_TYPE__CMR_FIELD_GROUP_MAPPING = 3;

    /**
     * The number of structural features of the the '<em>Prefetch Group Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int PREFETCH_GROUP_TYPE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.QueryMethodTypeImpl <em>Query Method Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.QueryMethodTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getQueryMethodType()
     * @generated
     */
    int QUERY_METHOD_TYPE = 19;

    /**
     * The feature id for the '<em><b>Method Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int QUERY_METHOD_TYPE__METHOD_NAME = 0;

    /**
     * The feature id for the '<em><b>Method Params</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int QUERY_METHOD_TYPE__METHOD_PARAMS = 1;

    /**
     * The number of structural features of the the '<em>Query Method Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int QUERY_METHOD_TYPE_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.QueryTypeImpl <em>Query Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.QueryTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getQueryType()
     * @generated
     */
    int QUERY_TYPE = 20;

    /**
     * The feature id for the '<em><b>Query Method</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int QUERY_TYPE__QUERY_METHOD = 0;

    /**
     * The feature id for the '<em><b>Result Type Mapping</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int QUERY_TYPE__RESULT_TYPE_MAPPING = 1;

    /**
     * The feature id for the '<em><b>Ejb Ql</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int QUERY_TYPE__EJB_QL = 2;

    /**
     * The feature id for the '<em><b>No Cache Flush</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int QUERY_TYPE__NO_CACHE_FLUSH = 3;

    /**
     * The feature id for the '<em><b>Group Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int QUERY_TYPE__GROUP_NAME = 4;

    /**
     * The number of structural features of the the '<em>Query Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int QUERY_TYPE_FEATURE_COUNT = 5;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.RelationshipRoleSourceTypeImpl <em>Relationship Role Source Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.RelationshipRoleSourceTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getRelationshipRoleSourceType()
     * @generated
     */
    int RELATIONSHIP_ROLE_SOURCE_TYPE = 21;

    /**
     * The feature id for the '<em><b>Ejb Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RELATIONSHIP_ROLE_SOURCE_TYPE__EJB_NAME = 0;

    /**
     * The number of structural features of the the '<em>Relationship Role Source Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RELATIONSHIP_ROLE_SOURCE_TYPE_FEATURE_COUNT = 1;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.RelationshipsTypeImpl <em>Relationships Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.RelationshipsTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getRelationshipsType()
     * @generated
     */
    int RELATIONSHIPS_TYPE = 22;

    /**
     * The feature id for the '<em><b>Ejb Relation</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RELATIONSHIPS_TYPE__EJB_RELATION = 0;

    /**
     * The number of structural features of the the '<em>Relationships Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int RELATIONSHIPS_TYPE_FEATURE_COUNT = 1;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.RoleMappingTypeImpl <em>Role Mapping Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.RoleMappingTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getRoleMappingType()
     * @generated
     */
    int ROLE_MAPPING_TYPE = 23;

    /**
     * The feature id for the '<em><b>Cmr Field Mapping</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ROLE_MAPPING_TYPE__CMR_FIELD_MAPPING = 0;

    /**
     * The number of structural features of the the '<em>Role Mapping Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int ROLE_MAPPING_TYPE_FEATURE_COUNT = 1;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl <em>Session Bean Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.SessionBeanTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getSessionBeanType()
     * @generated
     */
    int SESSION_BEAN_TYPE = 24;

    /**
     * The feature id for the '<em><b>Ejb Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__EJB_NAME = 0;

    /**
     * The feature id for the '<em><b>Jndi Name</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__JNDI_NAME = 1;

    /**
     * The feature id for the '<em><b>Local Jndi Name</b></em>' attribute list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__LOCAL_JNDI_NAME = 2;

    /**
     * The feature id for the '<em><b>Tss Target Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__TSS_TARGET_NAME = 3;

    /**
     * The feature id for the '<em><b>Tss Link</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__TSS_LINK = 4;

    /**
     * The feature id for the '<em><b>Tss</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__TSS = 5;

    /**
     * The feature id for the '<em><b>Ejb Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__EJB_REF = 6;

    /**
     * The feature id for the '<em><b>Ejb Local Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__EJB_LOCAL_REF = 7;

    /**
     * The feature id for the '<em><b>Service Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__SERVICE_REF = 8;

    /**
     * The feature id for the '<em><b>Resource Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__RESOURCE_REF = 9;

    /**
     * The feature id for the '<em><b>Resource Env Ref</b></em>' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__RESOURCE_ENV_REF = 10;

    /**
     * The feature id for the '<em><b>Web Service Address</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__WEB_SERVICE_ADDRESS = 11;

    /**
     * The feature id for the '<em><b>Web Service Security</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__WEB_SERVICE_SECURITY = 12;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE__ID = 13;

    /**
     * The number of structural features of the the '<em>Session Bean Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int SESSION_BEAN_TYPE_FEATURE_COUNT = 14;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.TssTypeImpl <em>Tss Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.TssTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getTssType()
     * @generated
     */
    int TSS_TYPE = 25;

    /**
     * The feature id for the '<em><b>Domain</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int TSS_TYPE__DOMAIN = 0;

    /**
     * The feature id for the '<em><b>Server</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int TSS_TYPE__SERVER = 1;

    /**
     * The feature id for the '<em><b>Application</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int TSS_TYPE__APPLICATION = 2;

    /**
     * The feature id for the '<em><b>Module</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int TSS_TYPE__MODULE = 3;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int TSS_TYPE__NAME = 4;

    /**
     * The number of structural features of the the '<em>Tss Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int TSS_TYPE_FEATURE_COUNT = 5;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.impl.WebServiceSecurityTypeImpl <em>Web Service Security Type</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.impl.WebServiceSecurityTypeImpl
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getWebServiceSecurityType()
     * @generated
     */
    int WEB_SERVICE_SECURITY_TYPE = 26;

    /**
     * The feature id for the '<em><b>Security Realm Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_SERVICE_SECURITY_TYPE__SECURITY_REALM_NAME = 0;

    /**
     * The feature id for the '<em><b>Realm Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_SERVICE_SECURITY_TYPE__REALM_NAME = 1;

    /**
     * The feature id for the '<em><b>Transport Guarantee</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_SERVICE_SECURITY_TYPE__TRANSPORT_GUARANTEE = 2;

    /**
     * The feature id for the '<em><b>Auth Method</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_SERVICE_SECURITY_TYPE__AUTH_METHOD = 3;

    /**
     * The number of structural features of the the '<em>Web Service Security Type</em>' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @ordered
     */
    int WEB_SERVICE_SECURITY_TYPE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '{@link org.openejb.xml.ns.openejb.jar.TransportGuaranteeType <em>Transport Guarantee Type</em>}' enum.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.TransportGuaranteeType
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getTransportGuaranteeType()
     * @generated
     */
    int TRANSPORT_GUARANTEE_TYPE = 27;

    /**
     * The meta object id for the '<em>Auth Method Type</em>' data type.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see java.lang.String
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getAuthMethodType()
     * @generated
     */
    int AUTH_METHOD_TYPE = 28;

    /**
     * The meta object id for the '<em>Transport Guarantee Type Object</em>' data type.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.openejb.xml.ns.openejb.jar.TransportGuaranteeType
     * @see org.openejb.xml.ns.openejb.jar.impl.JarPackageImpl#getTransportGuaranteeTypeObject()
     * @generated
     */
    int TRANSPORT_GUARANTEE_TYPE_OBJECT = 29;


    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType <em>Activation Config Property Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Activation Config Property Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType
     * @generated
     */
    EClass getActivationConfigPropertyType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType#getActivationConfigPropertyName <em>Activation Config Property Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Activation Config Property Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType#getActivationConfigPropertyName()
     * @see #getActivationConfigPropertyType()
     * @generated
     */
    EAttribute getActivationConfigPropertyType_ActivationConfigPropertyName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType#getActivationConfigPropertyValue <em>Activation Config Property Value</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Activation Config Property Value</em>'.
     * @see org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType#getActivationConfigPropertyValue()
     * @see #getActivationConfigPropertyType()
     * @generated
     */
    EAttribute getActivationConfigPropertyType_ActivationConfigPropertyValue();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigType <em>Activation Config Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Activation Config Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.ActivationConfigType
     * @generated
     */
    EClass getActivationConfigType();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigType#getDescription <em>Description</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Description</em>'.
     * @see org.openejb.xml.ns.openejb.jar.ActivationConfigType#getDescription()
     * @see #getActivationConfigType()
     * @generated
     */
    EAttribute getActivationConfigType_Description();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigType#getActivationConfigProperty <em>Activation Config Property</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Activation Config Property</em>'.
     * @see org.openejb.xml.ns.openejb.jar.ActivationConfigType#getActivationConfigProperty()
     * @see #getActivationConfigType()
     * @generated
     */
    EReference getActivationConfigType_ActivationConfigProperty();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType <em>Cmp Field Group Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Cmp Field Group Mapping Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType
     * @generated
     */
    EClass getCmpFieldGroupMappingType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType#getGroupName <em>Group Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Group Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType#getGroupName()
     * @see #getCmpFieldGroupMappingType()
     * @generated
     */
    EAttribute getCmpFieldGroupMappingType_GroupName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType#getCmpFieldName <em>Cmp Field Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Cmp Field Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType#getCmpFieldName()
     * @see #getCmpFieldGroupMappingType()
     * @generated
     */
    EAttribute getCmpFieldGroupMappingType_CmpFieldName();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType <em>Cmp Field Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Cmp Field Mapping Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldMappingType
     * @generated
     */
    EClass getCmpFieldMappingType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getCmpFieldName <em>Cmp Field Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Cmp Field Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getCmpFieldName()
     * @see #getCmpFieldMappingType()
     * @generated
     */
    EAttribute getCmpFieldMappingType_CmpFieldName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getCmpFieldClass <em>Cmp Field Class</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Cmp Field Class</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getCmpFieldClass()
     * @see #getCmpFieldMappingType()
     * @generated
     */
    EAttribute getCmpFieldMappingType_CmpFieldClass();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getTableColumn <em>Table Column</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Table Column</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getTableColumn()
     * @see #getCmpFieldMappingType()
     * @generated
     */
    EAttribute getCmpFieldMappingType_TableColumn();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getSqlType <em>Sql Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Sql Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getSqlType()
     * @see #getCmpFieldMappingType()
     * @generated
     */
    EAttribute getCmpFieldMappingType_SqlType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getTypeConverter <em>Type Converter</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Type Converter</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldMappingType#getTypeConverter()
     * @see #getCmpFieldMappingType()
     * @generated
     */
    EAttribute getCmpFieldMappingType_TypeConverter();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType <em>Cmr Field Group Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Cmr Field Group Mapping Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType
     * @generated
     */
    EClass getCmrFieldGroupMappingType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType#getGroupName <em>Group Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Group Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType#getGroupName()
     * @see #getCmrFieldGroupMappingType()
     * @generated
     */
    EAttribute getCmrFieldGroupMappingType_GroupName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType#getCmrFieldName <em>Cmr Field Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Cmr Field Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType#getCmrFieldName()
     * @see #getCmrFieldGroupMappingType()
     * @generated
     */
    EAttribute getCmrFieldGroupMappingType_CmrFieldName();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.CmrFieldMappingType <em>Cmr Field Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Cmr Field Mapping Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldMappingType
     * @generated
     */
    EClass getCmrFieldMappingType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmrFieldMappingType#getKeyColumn <em>Key Column</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Key Column</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldMappingType#getKeyColumn()
     * @see #getCmrFieldMappingType()
     * @generated
     */
    EAttribute getCmrFieldMappingType_KeyColumn();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmrFieldMappingType#getForeignKeyColumn <em>Foreign Key Column</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Foreign Key Column</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldMappingType#getForeignKeyColumn()
     * @see #getCmrFieldMappingType()
     * @generated
     */
    EAttribute getCmrFieldMappingType_ForeignKeyColumn();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.CmrFieldType <em>Cmr Field Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Cmr Field Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldType
     * @generated
     */
    EClass getCmrFieldType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmrFieldType#getCmrFieldName <em>Cmr Field Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Cmr Field Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldType#getCmrFieldName()
     * @see #getCmrFieldType()
     * @generated
     */
    EAttribute getCmrFieldType_CmrFieldName();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.CmrFieldType1 <em>Cmr Field Type1</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Cmr Field Type1</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldType1
     * @generated
     */
    EClass getCmrFieldType1();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmrFieldType1#getCmrFieldName <em>Cmr Field Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Cmr Field Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldType1#getCmrFieldName()
     * @see #getCmrFieldType1()
     * @generated
     */
    EAttribute getCmrFieldType1_CmrFieldName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.CmrFieldType1#getGroupName <em>Group Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Group Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldType1#getGroupName()
     * @see #getCmrFieldType1()
     * @generated
     */
    EAttribute getCmrFieldType1_GroupName();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Document Root</em>'.
     * @see org.openejb.xml.ns.openejb.jar.DocumentRoot
     * @generated
     */
    EClass getDocumentRoot();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.openejb.jar.DocumentRoot#getMixed <em>Mixed</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Mixed</em>'.
     * @see org.openejb.xml.ns.openejb.jar.DocumentRoot#getMixed()
     * @see #getDocumentRoot()
     * @generated
     */
    EAttribute getDocumentRoot_Mixed();

    /**
     * Returns the meta object for the map '{@link org.openejb.xml.ns.openejb.jar.DocumentRoot#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XMLNS Prefix Map</em>'.
     * @see org.openejb.xml.ns.openejb.jar.DocumentRoot#getXMLNSPrefixMap()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XMLNSPrefixMap();

    /**
     * Returns the meta object for the map '{@link org.openejb.xml.ns.openejb.jar.DocumentRoot#getXSISchemaLocation <em>XSI Schema Location</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the map '<em>XSI Schema Location</em>'.
     * @see org.openejb.xml.ns.openejb.jar.DocumentRoot#getXSISchemaLocation()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_XSISchemaLocation();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.DocumentRoot#getOpenejbJar <em>Openejb Jar</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Openejb Jar</em>'.
     * @see org.openejb.xml.ns.openejb.jar.DocumentRoot#getOpenejbJar()
     * @see #getDocumentRoot()
     * @generated
     */
    EReference getDocumentRoot_OpenejbJar();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType <em>Ejb Relationship Role Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Ejb Relationship Role Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType
     * @generated
     */
    EClass getEjbRelationshipRoleType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getEjbRelationshipRoleName <em>Ejb Relationship Role Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ejb Relationship Role Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getEjbRelationshipRoleName()
     * @see #getEjbRelationshipRoleType()
     * @generated
     */
    EAttribute getEjbRelationshipRoleType_EjbRelationshipRoleName();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getRelationshipRoleSource <em>Relationship Role Source</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Relationship Role Source</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getRelationshipRoleSource()
     * @see #getEjbRelationshipRoleType()
     * @generated
     */
    EReference getEjbRelationshipRoleType_RelationshipRoleSource();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getCmrField <em>Cmr Field</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Cmr Field</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getCmrField()
     * @see #getEjbRelationshipRoleType()
     * @generated
     */
    EReference getEjbRelationshipRoleType_CmrField();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getForeignKeyColumnOnSource <em>Foreign Key Column On Source</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Foreign Key Column On Source</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getForeignKeyColumnOnSource()
     * @see #getEjbRelationshipRoleType()
     * @generated
     */
    EReference getEjbRelationshipRoleType_ForeignKeyColumnOnSource();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getRoleMapping <em>Role Mapping</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Role Mapping</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType#getRoleMapping()
     * @see #getEjbRelationshipRoleType()
     * @generated
     */
    EReference getEjbRelationshipRoleType_RoleMapping();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.EjbRelationType <em>Ejb Relation Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Ejb Relation Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationType
     * @generated
     */
    EClass getEjbRelationType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EjbRelationType#getEjbRelationName <em>Ejb Relation Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ejb Relation Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationType#getEjbRelationName()
     * @see #getEjbRelationType()
     * @generated
     */
    EAttribute getEjbRelationType_EjbRelationName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EjbRelationType#getManyToManyTableName <em>Many To Many Table Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Many To Many Table Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationType#getManyToManyTableName()
     * @see #getEjbRelationType()
     * @generated
     */
    EAttribute getEjbRelationType_ManyToManyTableName();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EjbRelationType#getEjbRelationshipRole <em>Ejb Relationship Role</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Relationship Role</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationType#getEjbRelationshipRole()
     * @see #getEjbRelationType()
     * @generated
     */
    EReference getEjbRelationType_EjbRelationshipRole();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType <em>Enterprise Beans Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Enterprise Beans Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EnterpriseBeansType
     * @generated
     */
    EClass getEnterpriseBeansType();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getGroup <em>Group</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Group</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getGroup()
     * @see #getEnterpriseBeansType()
     * @generated
     */
    EAttribute getEnterpriseBeansType_Group();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getSession <em>Session</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Session</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getSession()
     * @see #getEnterpriseBeansType()
     * @generated
     */
    EReference getEnterpriseBeansType_Session();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getEntity <em>Entity</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Entity</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getEntity()
     * @see #getEnterpriseBeansType()
     * @generated
     */
    EReference getEnterpriseBeansType_Entity();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getMessageDriven <em>Message Driven</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Message Driven</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EnterpriseBeansType#getMessageDriven()
     * @see #getEnterpriseBeansType()
     * @generated
     */
    EReference getEnterpriseBeansType_MessageDriven();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType <em>Entity Bean Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Entity Bean Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType
     * @generated
     */
    EClass getEntityBeanType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getEjbName <em>Ejb Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ejb Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getEjbName()
     * @see #getEntityBeanType()
     * @generated
     */
    EAttribute getEntityBeanType_EjbName();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getJndiName <em>Jndi Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Jndi Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getJndiName()
     * @see #getEntityBeanType()
     * @generated
     */
    EAttribute getEntityBeanType_JndiName();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getLocalJndiName <em>Local Jndi Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Local Jndi Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getLocalJndiName()
     * @see #getEntityBeanType()
     * @generated
     */
    EAttribute getEntityBeanType_LocalJndiName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getTssTargetName <em>Tss Target Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Tss Target Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getTssTargetName()
     * @see #getEntityBeanType()
     * @generated
     */
    EAttribute getEntityBeanType_TssTargetName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getTssLink <em>Tss Link</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Tss Link</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getTssLink()
     * @see #getEntityBeanType()
     * @generated
     */
    EAttribute getEntityBeanType_TssLink();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getTss <em>Tss</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Tss</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getTss()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_Tss();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getTableName <em>Table Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Table Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getTableName()
     * @see #getEntityBeanType()
     * @generated
     */
    EAttribute getEntityBeanType_TableName();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getCmpFieldMapping <em>Cmp Field Mapping</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Cmp Field Mapping</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getCmpFieldMapping()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_CmpFieldMapping();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getPrimkeyField <em>Primkey Field</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Primkey Field</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getPrimkeyField()
     * @see #getEntityBeanType()
     * @generated
     */
    EAttribute getEntityBeanType_PrimkeyField();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getKeyGenerator <em>Key Generator</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Key Generator</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getKeyGenerator()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_KeyGenerator();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getPrefetchGroup <em>Prefetch Group</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Prefetch Group</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getPrefetchGroup()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_PrefetchGroup();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getEjbRef <em>Ejb Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getEjbRef()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_EjbRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getEjbLocalRef <em>Ejb Local Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Local Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getEjbLocalRef()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_EjbLocalRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getServiceRef <em>Service Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Service Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getServiceRef()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_ServiceRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getResourceRef <em>Resource Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Resource Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getResourceRef()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_ResourceRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getResourceEnvRef <em>Resource Env Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Resource Env Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getResourceEnvRef()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_ResourceEnvRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getQuery <em>Query</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Query</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getQuery()
     * @see #getEntityBeanType()
     * @generated
     */
    EReference getEntityBeanType_Query();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType#getId <em>Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Id</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType#getId()
     * @see #getEntityBeanType()
     * @generated
     */
    EAttribute getEntityBeanType_Id();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.EntityGroupMappingType <em>Entity Group Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Entity Group Mapping Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityGroupMappingType
     * @generated
     */
    EClass getEntityGroupMappingType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.EntityGroupMappingType#getGroupName <em>Group Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Group Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.EntityGroupMappingType#getGroupName()
     * @see #getEntityGroupMappingType()
     * @generated
     */
    EAttribute getEntityGroupMappingType_GroupName();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.GroupType <em>Group Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Group Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.GroupType
     * @generated
     */
    EClass getGroupType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.GroupType#getGroupName <em>Group Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Group Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.GroupType#getGroupName()
     * @see #getGroupType()
     * @generated
     */
    EAttribute getGroupType_GroupName();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.openejb.jar.GroupType#getCmpFieldName <em>Cmp Field Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Cmp Field Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.GroupType#getCmpFieldName()
     * @see #getGroupType()
     * @generated
     */
    EAttribute getGroupType_CmpFieldName();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.GroupType#getCmrField <em>Cmr Field</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Cmr Field</em>'.
     * @see org.openejb.xml.ns.openejb.jar.GroupType#getCmrField()
     * @see #getGroupType()
     * @generated
     */
    EReference getGroupType_CmrField();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType <em>Message Driven Bean Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Message Driven Bean Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType
     * @generated
     */
    EClass getMessageDrivenBeanType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbName <em>Ejb Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ejb Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbName()
     * @see #getMessageDrivenBeanType()
     * @generated
     */
    EAttribute getMessageDrivenBeanType_EjbName();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceAdapter <em>Resource Adapter</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Resource Adapter</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceAdapter()
     * @see #getMessageDrivenBeanType()
     * @generated
     */
    EReference getMessageDrivenBeanType_ResourceAdapter();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getActivationConfig <em>Activation Config</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Activation Config</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getActivationConfig()
     * @see #getMessageDrivenBeanType()
     * @generated
     */
    EReference getMessageDrivenBeanType_ActivationConfig();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbRef <em>Ejb Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbRef()
     * @see #getMessageDrivenBeanType()
     * @generated
     */
    EReference getMessageDrivenBeanType_EjbRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbLocalRef <em>Ejb Local Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Local Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getEjbLocalRef()
     * @see #getMessageDrivenBeanType()
     * @generated
     */
    EReference getMessageDrivenBeanType_EjbLocalRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getServiceRef <em>Service Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Service Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getServiceRef()
     * @see #getMessageDrivenBeanType()
     * @generated
     */
    EReference getMessageDrivenBeanType_ServiceRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceRef <em>Resource Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Resource Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceRef()
     * @see #getMessageDrivenBeanType()
     * @generated
     */
    EReference getMessageDrivenBeanType_ResourceRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceEnvRef <em>Resource Env Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Resource Env Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getResourceEnvRef()
     * @see #getMessageDrivenBeanType()
     * @generated
     */
    EReference getMessageDrivenBeanType_ResourceEnvRef();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getId <em>Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Id</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType#getId()
     * @see #getMessageDrivenBeanType()
     * @generated
     */
    EAttribute getMessageDrivenBeanType_Id();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.MethodParamsType <em>Method Params Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Method Params Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MethodParamsType
     * @generated
     */
    EClass getMethodParamsType();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.openejb.jar.MethodParamsType#getMethodParam <em>Method Param</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Method Param</em>'.
     * @see org.openejb.xml.ns.openejb.jar.MethodParamsType#getMethodParam()
     * @see #getMethodParamsType()
     * @generated
     */
    EAttribute getMethodParamsType_MethodParam();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType <em>Openejb Jar Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Openejb Jar Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType
     * @generated
     */
    EClass getOpenejbJarType();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getDependency <em>Dependency</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Dependency</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getDependency()
     * @see #getOpenejbJarType()
     * @generated
     */
    EReference getOpenejbJarType_Dependency();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getCmpConnectionFactory <em>Cmp Connection Factory</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Cmp Connection Factory</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getCmpConnectionFactory()
     * @see #getOpenejbJarType()
     * @generated
     */
    EReference getOpenejbJarType_CmpConnectionFactory();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEjbQlCompilerFactory <em>Ejb Ql Compiler Factory</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Ejb Ql Compiler Factory</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEjbQlCompilerFactory()
     * @see #getOpenejbJarType()
     * @generated
     */
    EReference getOpenejbJarType_EjbQlCompilerFactory();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getDbSyntaxFactory <em>Db Syntax Factory</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Db Syntax Factory</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getDbSyntaxFactory()
     * @see #getOpenejbJarType()
     * @generated
     */
    EReference getOpenejbJarType_DbSyntaxFactory();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEnforceForeignKeyConstraints <em>Enforce Foreign Key Constraints</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Enforce Foreign Key Constraints</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEnforceForeignKeyConstraints()
     * @see #getOpenejbJarType()
     * @generated
     */
    EReference getOpenejbJarType_EnforceForeignKeyConstraints();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEnterpriseBeans <em>Enterprise Beans</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Enterprise Beans</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getEnterpriseBeans()
     * @see #getOpenejbJarType()
     * @generated
     */
    EReference getOpenejbJarType_EnterpriseBeans();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getRelationships <em>Relationships</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Relationships</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getRelationships()
     * @see #getOpenejbJarType()
     * @generated
     */
    EReference getOpenejbJarType_Relationships();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getSecurity <em>Security</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Security</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getSecurity()
     * @see #getOpenejbJarType()
     * @generated
     */
    EReference getOpenejbJarType_Security();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getGbean <em>Gbean</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Gbean</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getGbean()
     * @see #getOpenejbJarType()
     * @generated
     */
    EReference getOpenejbJarType_Gbean();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getConfigId <em>Config Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Config Id</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getConfigId()
     * @see #getOpenejbJarType()
     * @generated
     */
    EAttribute getOpenejbJarType_ConfigId();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType#getParentId <em>Parent Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Parent Id</em>'.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType#getParentId()
     * @see #getOpenejbJarType()
     * @generated
     */
    EAttribute getOpenejbJarType_ParentId();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType <em>Prefetch Group Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Prefetch Group Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.PrefetchGroupType
     * @generated
     */
    EClass getPrefetchGroupType();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getGroup <em>Group</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Group</em>'.
     * @see org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getGroup()
     * @see #getPrefetchGroupType()
     * @generated
     */
    EReference getPrefetchGroupType_Group();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getEntityGroupMapping <em>Entity Group Mapping</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Entity Group Mapping</em>'.
     * @see org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getEntityGroupMapping()
     * @see #getPrefetchGroupType()
     * @generated
     */
    EReference getPrefetchGroupType_EntityGroupMapping();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getCmpFieldGroupMapping <em>Cmp Field Group Mapping</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Cmp Field Group Mapping</em>'.
     * @see org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getCmpFieldGroupMapping()
     * @see #getPrefetchGroupType()
     * @generated
     */
    EReference getPrefetchGroupType_CmpFieldGroupMapping();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getCmrFieldGroupMapping <em>Cmr Field Group Mapping</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Cmr Field Group Mapping</em>'.
     * @see org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getCmrFieldGroupMapping()
     * @see #getPrefetchGroupType()
     * @generated
     */
    EReference getPrefetchGroupType_CmrFieldGroupMapping();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.QueryMethodType <em>Query Method Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Query Method Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.QueryMethodType
     * @generated
     */
    EClass getQueryMethodType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.QueryMethodType#getMethodName <em>Method Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Method Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.QueryMethodType#getMethodName()
     * @see #getQueryMethodType()
     * @generated
     */
    EAttribute getQueryMethodType_MethodName();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.QueryMethodType#getMethodParams <em>Method Params</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Method Params</em>'.
     * @see org.openejb.xml.ns.openejb.jar.QueryMethodType#getMethodParams()
     * @see #getQueryMethodType()
     * @generated
     */
    EReference getQueryMethodType_MethodParams();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.QueryType <em>Query Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Query Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.QueryType
     * @generated
     */
    EClass getQueryType();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.QueryType#getQueryMethod <em>Query Method</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Query Method</em>'.
     * @see org.openejb.xml.ns.openejb.jar.QueryType#getQueryMethod()
     * @see #getQueryType()
     * @generated
     */
    EReference getQueryType_QueryMethod();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.QueryType#getResultTypeMapping <em>Result Type Mapping</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Result Type Mapping</em>'.
     * @see org.openejb.xml.ns.openejb.jar.QueryType#getResultTypeMapping()
     * @see #getQueryType()
     * @generated
     */
    EAttribute getQueryType_ResultTypeMapping();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.QueryType#getEjbQl <em>Ejb Ql</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ejb Ql</em>'.
     * @see org.openejb.xml.ns.openejb.jar.QueryType#getEjbQl()
     * @see #getQueryType()
     * @generated
     */
    EAttribute getQueryType_EjbQl();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.QueryType#getNoCacheFlush <em>No Cache Flush</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>No Cache Flush</em>'.
     * @see org.openejb.xml.ns.openejb.jar.QueryType#getNoCacheFlush()
     * @see #getQueryType()
     * @generated
     */
    EReference getQueryType_NoCacheFlush();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.QueryType#getGroupName <em>Group Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Group Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.QueryType#getGroupName()
     * @see #getQueryType()
     * @generated
     */
    EAttribute getQueryType_GroupName();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.RelationshipRoleSourceType <em>Relationship Role Source Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Relationship Role Source Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.RelationshipRoleSourceType
     * @generated
     */
    EClass getRelationshipRoleSourceType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.RelationshipRoleSourceType#getEjbName <em>Ejb Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ejb Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.RelationshipRoleSourceType#getEjbName()
     * @see #getRelationshipRoleSourceType()
     * @generated
     */
    EAttribute getRelationshipRoleSourceType_EjbName();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.RelationshipsType <em>Relationships Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Relationships Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.RelationshipsType
     * @generated
     */
    EClass getRelationshipsType();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.RelationshipsType#getEjbRelation <em>Ejb Relation</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Relation</em>'.
     * @see org.openejb.xml.ns.openejb.jar.RelationshipsType#getEjbRelation()
     * @see #getRelationshipsType()
     * @generated
     */
    EReference getRelationshipsType_EjbRelation();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.RoleMappingType <em>Role Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Role Mapping Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.RoleMappingType
     * @generated
     */
    EClass getRoleMappingType();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.RoleMappingType#getCmrFieldMapping <em>Cmr Field Mapping</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Cmr Field Mapping</em>'.
     * @see org.openejb.xml.ns.openejb.jar.RoleMappingType#getCmrFieldMapping()
     * @see #getRoleMappingType()
     * @generated
     */
    EReference getRoleMappingType_CmrFieldMapping();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType <em>Session Bean Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Session Bean Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType
     * @generated
     */
    EClass getSessionBeanType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbName <em>Ejb Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Ejb Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbName()
     * @see #getSessionBeanType()
     * @generated
     */
    EAttribute getSessionBeanType_EjbName();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getJndiName <em>Jndi Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Jndi Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getJndiName()
     * @see #getSessionBeanType()
     * @generated
     */
    EAttribute getSessionBeanType_JndiName();

    /**
     * Returns the meta object for the attribute list '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getLocalJndiName <em>Local Jndi Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute list '<em>Local Jndi Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getLocalJndiName()
     * @see #getSessionBeanType()
     * @generated
     */
    EAttribute getSessionBeanType_LocalJndiName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getTssTargetName <em>Tss Target Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Tss Target Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getTssTargetName()
     * @see #getSessionBeanType()
     * @generated
     */
    EAttribute getSessionBeanType_TssTargetName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getTssLink <em>Tss Link</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Tss Link</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getTssLink()
     * @see #getSessionBeanType()
     * @generated
     */
    EAttribute getSessionBeanType_TssLink();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getTss <em>Tss</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Tss</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getTss()
     * @see #getSessionBeanType()
     * @generated
     */
    EReference getSessionBeanType_Tss();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbRef <em>Ejb Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbRef()
     * @see #getSessionBeanType()
     * @generated
     */
    EReference getSessionBeanType_EjbRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbLocalRef <em>Ejb Local Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Ejb Local Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getEjbLocalRef()
     * @see #getSessionBeanType()
     * @generated
     */
    EReference getSessionBeanType_EjbLocalRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getServiceRef <em>Service Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Service Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getServiceRef()
     * @see #getSessionBeanType()
     * @generated
     */
    EReference getSessionBeanType_ServiceRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getResourceRef <em>Resource Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Resource Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getResourceRef()
     * @see #getSessionBeanType()
     * @generated
     */
    EReference getSessionBeanType_ResourceRef();

    /**
     * Returns the meta object for the containment reference list '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getResourceEnvRef <em>Resource Env Ref</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference list '<em>Resource Env Ref</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getResourceEnvRef()
     * @see #getSessionBeanType()
     * @generated
     */
    EReference getSessionBeanType_ResourceEnvRef();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getWebServiceAddress <em>Web Service Address</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Web Service Address</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getWebServiceAddress()
     * @see #getSessionBeanType()
     * @generated
     */
    EAttribute getSessionBeanType_WebServiceAddress();

    /**
     * Returns the meta object for the containment reference '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getWebServiceSecurity <em>Web Service Security</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the containment reference '<em>Web Service Security</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getWebServiceSecurity()
     * @see #getSessionBeanType()
     * @generated
     */
    EReference getSessionBeanType_WebServiceSecurity();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType#getId <em>Id</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Id</em>'.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType#getId()
     * @see #getSessionBeanType()
     * @generated
     */
    EAttribute getSessionBeanType_Id();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.TssType <em>Tss Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Tss Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.TssType
     * @generated
     */
    EClass getTssType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.TssType#getDomain <em>Domain</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Domain</em>'.
     * @see org.openejb.xml.ns.openejb.jar.TssType#getDomain()
     * @see #getTssType()
     * @generated
     */
    EAttribute getTssType_Domain();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.TssType#getServer <em>Server</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Server</em>'.
     * @see org.openejb.xml.ns.openejb.jar.TssType#getServer()
     * @see #getTssType()
     * @generated
     */
    EAttribute getTssType_Server();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.TssType#getApplication <em>Application</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Application</em>'.
     * @see org.openejb.xml.ns.openejb.jar.TssType#getApplication()
     * @see #getTssType()
     * @generated
     */
    EAttribute getTssType_Application();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.TssType#getModule <em>Module</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Module</em>'.
     * @see org.openejb.xml.ns.openejb.jar.TssType#getModule()
     * @see #getTssType()
     * @generated
     */
    EAttribute getTssType_Module();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.TssType#getName <em>Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.TssType#getName()
     * @see #getTssType()
     * @generated
     */
    EAttribute getTssType_Name();

    /**
     * Returns the meta object for class '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType <em>Web Service Security Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for class '<em>Web Service Security Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.WebServiceSecurityType
     * @generated
     */
    EClass getWebServiceSecurityType();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getSecurityRealmName <em>Security Realm Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Security Realm Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getSecurityRealmName()
     * @see #getWebServiceSecurityType()
     * @generated
     */
    EAttribute getWebServiceSecurityType_SecurityRealmName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getRealmName <em>Realm Name</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Realm Name</em>'.
     * @see org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getRealmName()
     * @see #getWebServiceSecurityType()
     * @generated
     */
    EAttribute getWebServiceSecurityType_RealmName();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getTransportGuarantee <em>Transport Guarantee</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Transport Guarantee</em>'.
     * @see org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getTransportGuarantee()
     * @see #getWebServiceSecurityType()
     * @generated
     */
    EAttribute getWebServiceSecurityType_TransportGuarantee();

    /**
     * Returns the meta object for the attribute '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getAuthMethod <em>Auth Method</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for the attribute '<em>Auth Method</em>'.
     * @see org.openejb.xml.ns.openejb.jar.WebServiceSecurityType#getAuthMethod()
     * @see #getWebServiceSecurityType()
     * @generated
     */
    EAttribute getWebServiceSecurityType_AuthMethod();

    /**
     * Returns the meta object for enum '{@link org.openejb.xml.ns.openejb.jar.TransportGuaranteeType <em>Transport Guarantee Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for enum '<em>Transport Guarantee Type</em>'.
     * @see org.openejb.xml.ns.openejb.jar.TransportGuaranteeType
     * @generated
     */
    EEnum getTransportGuaranteeType();

    /**
     * Returns the meta object for data type '{@link java.lang.String <em>Auth Method Type</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for data type '<em>Auth Method Type</em>'.
     * @see java.lang.String
     * @model instanceClass="java.lang.String"
     *        extendedMetaData="name='auth-methodType' baseType='http://www.eclipse.org/emf/2003/XMLType#string' enumeration='BASIC DIGEST CLIENT-CERT NONE'" 
     * @generated
     */
    EDataType getAuthMethodType();

    /**
     * Returns the meta object for data type '{@link org.openejb.xml.ns.openejb.jar.TransportGuaranteeType <em>Transport Guarantee Type Object</em>}'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the meta object for data type '<em>Transport Guarantee Type Object</em>'.
     * @see org.openejb.xml.ns.openejb.jar.TransportGuaranteeType
     * @model instanceClass="org.openejb.xml.ns.openejb.jar.TransportGuaranteeType"
     *        extendedMetaData="name='transport-guaranteeType:Object' baseType='transport-guaranteeType'" 
     * @generated
     */
    EDataType getTransportGuaranteeTypeObject();

    /**
     * Returns the factory that creates the instances of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the factory that creates the instances of the model.
     * @generated
     */
    JarFactory getJarFactory();

} //JarPackage
