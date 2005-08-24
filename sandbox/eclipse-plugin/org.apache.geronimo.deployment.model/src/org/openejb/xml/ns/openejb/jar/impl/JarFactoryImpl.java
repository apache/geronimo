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
package org.openejb.xml.ns.openejb.jar.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

import org.openejb.xml.ns.openejb.jar.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class JarFactoryImpl extends EFactoryImpl implements JarFactory {
    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public JarFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE: return createActivationConfigPropertyType();
            case JarPackage.ACTIVATION_CONFIG_TYPE: return createActivationConfigType();
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE: return createCmpFieldGroupMappingType();
            case JarPackage.CMP_FIELD_MAPPING_TYPE: return createCmpFieldMappingType();
            case JarPackage.CMR_FIELD_GROUP_MAPPING_TYPE: return createCmrFieldGroupMappingType();
            case JarPackage.CMR_FIELD_MAPPING_TYPE: return createCmrFieldMappingType();
            case JarPackage.CMR_FIELD_TYPE: return createCmrFieldType();
            case JarPackage.CMR_FIELD_TYPE1: return createCmrFieldType1();
            case JarPackage.DOCUMENT_ROOT: return createDocumentRoot();
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE: return createEjbRelationshipRoleType();
            case JarPackage.EJB_RELATION_TYPE: return createEjbRelationType();
            case JarPackage.ENTERPRISE_BEANS_TYPE: return createEnterpriseBeansType();
            case JarPackage.ENTITY_BEAN_TYPE: return createEntityBeanType();
            case JarPackage.ENTITY_GROUP_MAPPING_TYPE: return createEntityGroupMappingType();
            case JarPackage.GROUP_TYPE: return createGroupType();
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE: return createMessageDrivenBeanType();
            case JarPackage.METHOD_PARAMS_TYPE: return createMethodParamsType();
            case JarPackage.OPENEJB_JAR_TYPE: return createOpenejbJarType();
            case JarPackage.PREFETCH_GROUP_TYPE: return createPrefetchGroupType();
            case JarPackage.QUERY_METHOD_TYPE: return createQueryMethodType();
            case JarPackage.QUERY_TYPE: return createQueryType();
            case JarPackage.RELATIONSHIP_ROLE_SOURCE_TYPE: return createRelationshipRoleSourceType();
            case JarPackage.RELATIONSHIPS_TYPE: return createRelationshipsType();
            case JarPackage.ROLE_MAPPING_TYPE: return createRoleMappingType();
            case JarPackage.SESSION_BEAN_TYPE: return createSessionBeanType();
            case JarPackage.TSS_TYPE: return createTssType();
            case JarPackage.WEB_SERVICE_SECURITY_TYPE: return createWebServiceSecurityType();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Object createFromString(EDataType eDataType, String initialValue) {
        switch (eDataType.getClassifierID()) {
            case JarPackage.TRANSPORT_GUARANTEE_TYPE: {
                TransportGuaranteeType result = TransportGuaranteeType.get(initialValue);
                if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
                return result;
            }
            case JarPackage.AUTH_METHOD_TYPE:
                return createAuthMethodTypeFromString(eDataType, initialValue);
            case JarPackage.TRANSPORT_GUARANTEE_TYPE_OBJECT:
                return createTransportGuaranteeTypeObjectFromString(eDataType, initialValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertToString(EDataType eDataType, Object instanceValue) {
        switch (eDataType.getClassifierID()) {
            case JarPackage.TRANSPORT_GUARANTEE_TYPE:
                return instanceValue == null ? null : instanceValue.toString();
            case JarPackage.AUTH_METHOD_TYPE:
                return convertAuthMethodTypeToString(eDataType, instanceValue);
            case JarPackage.TRANSPORT_GUARANTEE_TYPE_OBJECT:
                return convertTransportGuaranteeTypeObjectToString(eDataType, instanceValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ActivationConfigPropertyType createActivationConfigPropertyType() {
        ActivationConfigPropertyTypeImpl activationConfigPropertyType = new ActivationConfigPropertyTypeImpl();
        return activationConfigPropertyType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ActivationConfigType createActivationConfigType() {
        ActivationConfigTypeImpl activationConfigType = new ActivationConfigTypeImpl();
        return activationConfigType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CmpFieldGroupMappingType createCmpFieldGroupMappingType() {
        CmpFieldGroupMappingTypeImpl cmpFieldGroupMappingType = new CmpFieldGroupMappingTypeImpl();
        return cmpFieldGroupMappingType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CmpFieldMappingType createCmpFieldMappingType() {
        CmpFieldMappingTypeImpl cmpFieldMappingType = new CmpFieldMappingTypeImpl();
        return cmpFieldMappingType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CmrFieldGroupMappingType createCmrFieldGroupMappingType() {
        CmrFieldGroupMappingTypeImpl cmrFieldGroupMappingType = new CmrFieldGroupMappingTypeImpl();
        return cmrFieldGroupMappingType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CmrFieldMappingType createCmrFieldMappingType() {
        CmrFieldMappingTypeImpl cmrFieldMappingType = new CmrFieldMappingTypeImpl();
        return cmrFieldMappingType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CmrFieldType createCmrFieldType() {
        CmrFieldTypeImpl cmrFieldType = new CmrFieldTypeImpl();
        return cmrFieldType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CmrFieldType1 createCmrFieldType1() {
        CmrFieldType1Impl cmrFieldType1 = new CmrFieldType1Impl();
        return cmrFieldType1;
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
    public EjbRelationshipRoleType createEjbRelationshipRoleType() {
        EjbRelationshipRoleTypeImpl ejbRelationshipRoleType = new EjbRelationshipRoleTypeImpl();
        return ejbRelationshipRoleType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EjbRelationType createEjbRelationType() {
        EjbRelationTypeImpl ejbRelationType = new EjbRelationTypeImpl();
        return ejbRelationType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EnterpriseBeansType createEnterpriseBeansType() {
        EnterpriseBeansTypeImpl enterpriseBeansType = new EnterpriseBeansTypeImpl();
        return enterpriseBeansType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EntityBeanType createEntityBeanType() {
        EntityBeanTypeImpl entityBeanType = new EntityBeanTypeImpl();
        return entityBeanType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EntityGroupMappingType createEntityGroupMappingType() {
        EntityGroupMappingTypeImpl entityGroupMappingType = new EntityGroupMappingTypeImpl();
        return entityGroupMappingType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GroupType createGroupType() {
        GroupTypeImpl groupType = new GroupTypeImpl();
        return groupType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MessageDrivenBeanType createMessageDrivenBeanType() {
        MessageDrivenBeanTypeImpl messageDrivenBeanType = new MessageDrivenBeanTypeImpl();
        return messageDrivenBeanType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MethodParamsType createMethodParamsType() {
        MethodParamsTypeImpl methodParamsType = new MethodParamsTypeImpl();
        return methodParamsType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public OpenejbJarType createOpenejbJarType() {
        OpenejbJarTypeImpl openejbJarType = new OpenejbJarTypeImpl();
        return openejbJarType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PrefetchGroupType createPrefetchGroupType() {
        PrefetchGroupTypeImpl prefetchGroupType = new PrefetchGroupTypeImpl();
        return prefetchGroupType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public QueryMethodType createQueryMethodType() {
        QueryMethodTypeImpl queryMethodType = new QueryMethodTypeImpl();
        return queryMethodType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public QueryType createQueryType() {
        QueryTypeImpl queryType = new QueryTypeImpl();
        return queryType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RelationshipRoleSourceType createRelationshipRoleSourceType() {
        RelationshipRoleSourceTypeImpl relationshipRoleSourceType = new RelationshipRoleSourceTypeImpl();
        return relationshipRoleSourceType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RelationshipsType createRelationshipsType() {
        RelationshipsTypeImpl relationshipsType = new RelationshipsTypeImpl();
        return relationshipsType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public RoleMappingType createRoleMappingType() {
        RoleMappingTypeImpl roleMappingType = new RoleMappingTypeImpl();
        return roleMappingType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SessionBeanType createSessionBeanType() {
        SessionBeanTypeImpl sessionBeanType = new SessionBeanTypeImpl();
        return sessionBeanType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public TssType createTssType() {
        TssTypeImpl tssType = new TssTypeImpl();
        return tssType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public WebServiceSecurityType createWebServiceSecurityType() {
        WebServiceSecurityTypeImpl webServiceSecurityType = new WebServiceSecurityTypeImpl();
        return webServiceSecurityType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String createAuthMethodTypeFromString(EDataType eDataType, String initialValue) {
        return (String)XMLTypeFactory.eINSTANCE.createFromString(XMLTypePackage.eINSTANCE.getString(), initialValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertAuthMethodTypeToString(EDataType eDataType, Object instanceValue) {
        return XMLTypeFactory.eINSTANCE.convertToString(XMLTypePackage.eINSTANCE.getString(), instanceValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public TransportGuaranteeType createTransportGuaranteeTypeObjectFromString(EDataType eDataType, String initialValue) {
        return (TransportGuaranteeType)JarFactory.eINSTANCE.createFromString(JarPackage.eINSTANCE.getTransportGuaranteeType(), initialValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertTransportGuaranteeTypeObjectToString(EDataType eDataType, Object instanceValue) {
        return JarFactory.eINSTANCE.convertToString(JarPackage.eINSTANCE.getTransportGuaranteeType(), instanceValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public JarPackage getJarPackage() {
        return (JarPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    public static JarPackage getPackage() {
        return JarPackage.eINSTANCE;
    }

} //JarFactoryImpl
