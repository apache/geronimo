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
package org.openejb.xml.ns.openejb.jar.util;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.openejb.xml.ns.openejb.jar.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.openejb.xml.ns.openejb.jar.JarPackage
 * @generated
 */
public class JarSwitch {
    /**
     * The cached model package
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static JarPackage modelPackage;

    /**
     * Creates an instance of the switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public JarSwitch() {
        if (modelPackage == null) {
            modelPackage = JarPackage.eINSTANCE;
        }
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    public Object doSwitch(EObject theEObject) {
        return doSwitch(theEObject.eClass(), theEObject);
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    protected Object doSwitch(EClass theEClass, EObject theEObject) {
        if (theEClass.eContainer() == modelPackage) {
            return doSwitch(theEClass.getClassifierID(), theEObject);
        }
        else {
            List eSuperTypes = theEClass.getESuperTypes();
            return
                eSuperTypes.isEmpty() ?
                    defaultCase(theEObject) :
                    doSwitch((EClass)eSuperTypes.get(0), theEObject);
        }
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
    protected Object doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE: {
                ActivationConfigPropertyType activationConfigPropertyType = (ActivationConfigPropertyType)theEObject;
                Object result = caseActivationConfigPropertyType(activationConfigPropertyType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.ACTIVATION_CONFIG_TYPE: {
                ActivationConfigType activationConfigType = (ActivationConfigType)theEObject;
                Object result = caseActivationConfigType(activationConfigType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE: {
                CmpFieldGroupMappingType cmpFieldGroupMappingType = (CmpFieldGroupMappingType)theEObject;
                Object result = caseCmpFieldGroupMappingType(cmpFieldGroupMappingType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.CMP_FIELD_MAPPING_TYPE: {
                CmpFieldMappingType cmpFieldMappingType = (CmpFieldMappingType)theEObject;
                Object result = caseCmpFieldMappingType(cmpFieldMappingType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.CMR_FIELD_GROUP_MAPPING_TYPE: {
                CmrFieldGroupMappingType cmrFieldGroupMappingType = (CmrFieldGroupMappingType)theEObject;
                Object result = caseCmrFieldGroupMappingType(cmrFieldGroupMappingType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.CMR_FIELD_MAPPING_TYPE: {
                CmrFieldMappingType cmrFieldMappingType = (CmrFieldMappingType)theEObject;
                Object result = caseCmrFieldMappingType(cmrFieldMappingType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.CMR_FIELD_TYPE: {
                CmrFieldType cmrFieldType = (CmrFieldType)theEObject;
                Object result = caseCmrFieldType(cmrFieldType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.CMR_FIELD_TYPE1: {
                CmrFieldType1 cmrFieldType1 = (CmrFieldType1)theEObject;
                Object result = caseCmrFieldType1(cmrFieldType1);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.DOCUMENT_ROOT: {
                DocumentRoot documentRoot = (DocumentRoot)theEObject;
                Object result = caseDocumentRoot(documentRoot);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE: {
                EjbRelationshipRoleType ejbRelationshipRoleType = (EjbRelationshipRoleType)theEObject;
                Object result = caseEjbRelationshipRoleType(ejbRelationshipRoleType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.EJB_RELATION_TYPE: {
                EjbRelationType ejbRelationType = (EjbRelationType)theEObject;
                Object result = caseEjbRelationType(ejbRelationType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.ENTERPRISE_BEANS_TYPE: {
                EnterpriseBeansType enterpriseBeansType = (EnterpriseBeansType)theEObject;
                Object result = caseEnterpriseBeansType(enterpriseBeansType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.ENTITY_BEAN_TYPE: {
                EntityBeanType entityBeanType = (EntityBeanType)theEObject;
                Object result = caseEntityBeanType(entityBeanType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.ENTITY_GROUP_MAPPING_TYPE: {
                EntityGroupMappingType entityGroupMappingType = (EntityGroupMappingType)theEObject;
                Object result = caseEntityGroupMappingType(entityGroupMappingType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.GROUP_TYPE: {
                GroupType groupType = (GroupType)theEObject;
                Object result = caseGroupType(groupType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE: {
                MessageDrivenBeanType messageDrivenBeanType = (MessageDrivenBeanType)theEObject;
                Object result = caseMessageDrivenBeanType(messageDrivenBeanType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.METHOD_PARAMS_TYPE: {
                MethodParamsType methodParamsType = (MethodParamsType)theEObject;
                Object result = caseMethodParamsType(methodParamsType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.OPENEJB_JAR_TYPE: {
                OpenejbJarType openejbJarType = (OpenejbJarType)theEObject;
                Object result = caseOpenejbJarType(openejbJarType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.PREFETCH_GROUP_TYPE: {
                PrefetchGroupType prefetchGroupType = (PrefetchGroupType)theEObject;
                Object result = casePrefetchGroupType(prefetchGroupType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.QUERY_METHOD_TYPE: {
                QueryMethodType queryMethodType = (QueryMethodType)theEObject;
                Object result = caseQueryMethodType(queryMethodType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.QUERY_TYPE: {
                QueryType queryType = (QueryType)theEObject;
                Object result = caseQueryType(queryType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.RELATIONSHIP_ROLE_SOURCE_TYPE: {
                RelationshipRoleSourceType relationshipRoleSourceType = (RelationshipRoleSourceType)theEObject;
                Object result = caseRelationshipRoleSourceType(relationshipRoleSourceType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.RELATIONSHIPS_TYPE: {
                RelationshipsType relationshipsType = (RelationshipsType)theEObject;
                Object result = caseRelationshipsType(relationshipsType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.ROLE_MAPPING_TYPE: {
                RoleMappingType roleMappingType = (RoleMappingType)theEObject;
                Object result = caseRoleMappingType(roleMappingType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.SESSION_BEAN_TYPE: {
                SessionBeanType sessionBeanType = (SessionBeanType)theEObject;
                Object result = caseSessionBeanType(sessionBeanType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.TSS_TYPE: {
                TssType tssType = (TssType)theEObject;
                Object result = caseTssType(tssType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case JarPackage.WEB_SERVICE_SECURITY_TYPE: {
                WebServiceSecurityType webServiceSecurityType = (WebServiceSecurityType)theEObject;
                Object result = caseWebServiceSecurityType(webServiceSecurityType);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            default: return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Activation Config Property Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Activation Config Property Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseActivationConfigPropertyType(ActivationConfigPropertyType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Activation Config Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Activation Config Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseActivationConfigType(ActivationConfigType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Cmp Field Group Mapping Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Cmp Field Group Mapping Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseCmpFieldGroupMappingType(CmpFieldGroupMappingType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Cmp Field Mapping Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Cmp Field Mapping Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseCmpFieldMappingType(CmpFieldMappingType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Cmr Field Group Mapping Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Cmr Field Group Mapping Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseCmrFieldGroupMappingType(CmrFieldGroupMappingType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Cmr Field Mapping Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Cmr Field Mapping Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseCmrFieldMappingType(CmrFieldMappingType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Cmr Field Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Cmr Field Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseCmrFieldType(CmrFieldType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Cmr Field Type1</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Cmr Field Type1</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseCmrFieldType1(CmrFieldType1 object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Document Root</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Document Root</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseDocumentRoot(DocumentRoot object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Ejb Relationship Role Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Ejb Relationship Role Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseEjbRelationshipRoleType(EjbRelationshipRoleType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Ejb Relation Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Ejb Relation Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseEjbRelationType(EjbRelationType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Enterprise Beans Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Enterprise Beans Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseEnterpriseBeansType(EnterpriseBeansType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Entity Bean Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Entity Bean Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseEntityBeanType(EntityBeanType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Entity Group Mapping Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Entity Group Mapping Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseEntityGroupMappingType(EntityGroupMappingType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Group Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Group Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseGroupType(GroupType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Message Driven Bean Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Message Driven Bean Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseMessageDrivenBeanType(MessageDrivenBeanType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Method Params Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Method Params Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseMethodParamsType(MethodParamsType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Openejb Jar Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Openejb Jar Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseOpenejbJarType(OpenejbJarType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Prefetch Group Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Prefetch Group Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object casePrefetchGroupType(PrefetchGroupType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Query Method Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Query Method Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseQueryMethodType(QueryMethodType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Query Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Query Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseQueryType(QueryType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Relationship Role Source Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Relationship Role Source Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseRelationshipRoleSourceType(RelationshipRoleSourceType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Relationships Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Relationships Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseRelationshipsType(RelationshipsType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Role Mapping Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Role Mapping Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseRoleMappingType(RoleMappingType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Session Bean Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Session Bean Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseSessionBeanType(SessionBeanType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Tss Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Tss Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseTssType(TssType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>Web Service Security Type</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>Web Service Security Type</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public Object caseWebServiceSecurityType(WebServiceSecurityType object) {
        return null;
    }

    /**
     * Returns the result of interpretting the object as an instance of '<em>EObject</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch, but this is the last case anyway.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpretting the object as an instance of '<em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
    public Object defaultCase(EObject object) {
        return null;
    }

} //JarSwitch
