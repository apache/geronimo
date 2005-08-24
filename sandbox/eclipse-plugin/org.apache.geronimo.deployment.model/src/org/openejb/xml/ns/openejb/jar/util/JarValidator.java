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

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.common.util.DiagnosticChain;

import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.EObjectValidator;

import org.eclipse.emf.ecore.xml.type.util.XMLTypeValidator;

import org.openejb.xml.ns.openejb.jar.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Validator</b> for the model.
 * <!-- end-user-doc -->
 * @see org.openejb.xml.ns.openejb.jar.JarPackage
 * @generated
 */
public class JarValidator extends EObjectValidator {
    /**
     * The cached model package
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final JarValidator INSTANCE = new JarValidator();

    /**
     * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.emf.common.util.Diagnostic#getSource()
     * @see org.eclipse.emf.common.util.Diagnostic#getCode()
     * @generated
     */
    public static final String DIAGNOSTIC_SOURCE = "org.openejb.xml.ns.openejb.jar";

    /**
     * A constant with a fixed name that can be used as the base value for additional hand written constants.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 0;

    /**
     * A constant with a fixed name that can be used as the base value for additional hand written constants in a derived class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;

    /**
     * The cached base package validator.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected XMLTypeValidator xmlTypeValidator;

    /**
     * Creates an instance of the switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public JarValidator() {
        super();
        xmlTypeValidator = XMLTypeValidator.INSTANCE;
    }

    /**
     * Returns the package of this validator switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EPackage getEPackage() {
      return JarPackage.eINSTANCE;
    }

    /**
     * Calls <code>validateXXX</code> for the corresonding classifier of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected boolean validate(int classifierID, Object value, DiagnosticChain diagnostics, Map context) {
        switch (classifierID) {
            case JarPackage.ACTIVATION_CONFIG_PROPERTY_TYPE:
                return validateActivationConfigPropertyType((ActivationConfigPropertyType)value, diagnostics, context);
            case JarPackage.ACTIVATION_CONFIG_TYPE:
                return validateActivationConfigType((ActivationConfigType)value, diagnostics, context);
            case JarPackage.CMP_FIELD_GROUP_MAPPING_TYPE:
                return validateCmpFieldGroupMappingType((CmpFieldGroupMappingType)value, diagnostics, context);
            case JarPackage.CMP_FIELD_MAPPING_TYPE:
                return validateCmpFieldMappingType((CmpFieldMappingType)value, diagnostics, context);
            case JarPackage.CMR_FIELD_GROUP_MAPPING_TYPE:
                return validateCmrFieldGroupMappingType((CmrFieldGroupMappingType)value, diagnostics, context);
            case JarPackage.CMR_FIELD_MAPPING_TYPE:
                return validateCmrFieldMappingType((CmrFieldMappingType)value, diagnostics, context);
            case JarPackage.CMR_FIELD_TYPE:
                return validateCmrFieldType((CmrFieldType)value, diagnostics, context);
            case JarPackage.CMR_FIELD_TYPE1:
                return validateCmrFieldType1((CmrFieldType1)value, diagnostics, context);
            case JarPackage.DOCUMENT_ROOT:
                return validateDocumentRoot((DocumentRoot)value, diagnostics, context);
            case JarPackage.EJB_RELATIONSHIP_ROLE_TYPE:
                return validateEjbRelationshipRoleType((EjbRelationshipRoleType)value, diagnostics, context);
            case JarPackage.EJB_RELATION_TYPE:
                return validateEjbRelationType((EjbRelationType)value, diagnostics, context);
            case JarPackage.ENTERPRISE_BEANS_TYPE:
                return validateEnterpriseBeansType((EnterpriseBeansType)value, diagnostics, context);
            case JarPackage.ENTITY_BEAN_TYPE:
                return validateEntityBeanType((EntityBeanType)value, diagnostics, context);
            case JarPackage.ENTITY_GROUP_MAPPING_TYPE:
                return validateEntityGroupMappingType((EntityGroupMappingType)value, diagnostics, context);
            case JarPackage.GROUP_TYPE:
                return validateGroupType((GroupType)value, diagnostics, context);
            case JarPackage.MESSAGE_DRIVEN_BEAN_TYPE:
                return validateMessageDrivenBeanType((MessageDrivenBeanType)value, diagnostics, context);
            case JarPackage.METHOD_PARAMS_TYPE:
                return validateMethodParamsType((MethodParamsType)value, diagnostics, context);
            case JarPackage.OPENEJB_JAR_TYPE:
                return validateOpenejbJarType((OpenejbJarType)value, diagnostics, context);
            case JarPackage.PREFETCH_GROUP_TYPE:
                return validatePrefetchGroupType((PrefetchGroupType)value, diagnostics, context);
            case JarPackage.QUERY_METHOD_TYPE:
                return validateQueryMethodType((QueryMethodType)value, diagnostics, context);
            case JarPackage.QUERY_TYPE:
                return validateQueryType((QueryType)value, diagnostics, context);
            case JarPackage.RELATIONSHIP_ROLE_SOURCE_TYPE:
                return validateRelationshipRoleSourceType((RelationshipRoleSourceType)value, diagnostics, context);
            case JarPackage.RELATIONSHIPS_TYPE:
                return validateRelationshipsType((RelationshipsType)value, diagnostics, context);
            case JarPackage.ROLE_MAPPING_TYPE:
                return validateRoleMappingType((RoleMappingType)value, diagnostics, context);
            case JarPackage.SESSION_BEAN_TYPE:
                return validateSessionBeanType((SessionBeanType)value, diagnostics, context);
            case JarPackage.TSS_TYPE:
                return validateTssType((TssType)value, diagnostics, context);
            case JarPackage.WEB_SERVICE_SECURITY_TYPE:
                return validateWebServiceSecurityType((WebServiceSecurityType)value, diagnostics, context);
            case JarPackage.TRANSPORT_GUARANTEE_TYPE:
                return validateTransportGuaranteeType((Object)value, diagnostics, context);
            case JarPackage.AUTH_METHOD_TYPE:
                return validateAuthMethodType((String)value, diagnostics, context);
            case JarPackage.TRANSPORT_GUARANTEE_TYPE_OBJECT:
                return validateTransportGuaranteeTypeObject((TransportGuaranteeType)value, diagnostics, context);
            default: 
                return true;
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateActivationConfigPropertyType(ActivationConfigPropertyType activationConfigPropertyType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(activationConfigPropertyType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateActivationConfigType(ActivationConfigType activationConfigType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(activationConfigType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateCmpFieldGroupMappingType(CmpFieldGroupMappingType cmpFieldGroupMappingType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(cmpFieldGroupMappingType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateCmpFieldMappingType(CmpFieldMappingType cmpFieldMappingType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(cmpFieldMappingType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateCmrFieldGroupMappingType(CmrFieldGroupMappingType cmrFieldGroupMappingType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(cmrFieldGroupMappingType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateCmrFieldMappingType(CmrFieldMappingType cmrFieldMappingType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(cmrFieldMappingType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateCmrFieldType(CmrFieldType cmrFieldType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(cmrFieldType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateCmrFieldType1(CmrFieldType1 cmrFieldType1, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(cmrFieldType1, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateDocumentRoot(DocumentRoot documentRoot, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(documentRoot, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateEjbRelationshipRoleType(EjbRelationshipRoleType ejbRelationshipRoleType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(ejbRelationshipRoleType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateEjbRelationType(EjbRelationType ejbRelationType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(ejbRelationType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateEnterpriseBeansType(EnterpriseBeansType enterpriseBeansType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(enterpriseBeansType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateEntityBeanType(EntityBeanType entityBeanType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(entityBeanType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateEntityGroupMappingType(EntityGroupMappingType entityGroupMappingType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(entityGroupMappingType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGroupType(GroupType groupType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(groupType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateMessageDrivenBeanType(MessageDrivenBeanType messageDrivenBeanType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(messageDrivenBeanType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateMethodParamsType(MethodParamsType methodParamsType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(methodParamsType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateOpenejbJarType(OpenejbJarType openejbJarType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(openejbJarType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validatePrefetchGroupType(PrefetchGroupType prefetchGroupType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(prefetchGroupType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateQueryMethodType(QueryMethodType queryMethodType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(queryMethodType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateQueryType(QueryType queryType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(queryType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateRelationshipRoleSourceType(RelationshipRoleSourceType relationshipRoleSourceType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(relationshipRoleSourceType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateRelationshipsType(RelationshipsType relationshipsType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(relationshipsType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateRoleMappingType(RoleMappingType roleMappingType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(roleMappingType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateSessionBeanType(SessionBeanType sessionBeanType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(sessionBeanType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateTssType(TssType tssType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(tssType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateWebServiceSecurityType(WebServiceSecurityType webServiceSecurityType, DiagnosticChain diagnostics, Map context) {
        return validate_EveryDefaultConstraint(webServiceSecurityType, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateTransportGuaranteeType(Object transportGuaranteeType, DiagnosticChain diagnostics, Map context) {
        return true;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateAuthMethodType(String authMethodType, DiagnosticChain diagnostics, Map context) {
        boolean result = validateAuthMethodType_Enumeration(authMethodType, diagnostics, context);
        return result;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     * @see #validateAuthMethodType_Enumeration
     */
    public static final Collection AUTH_METHOD_TYPE__ENUMERATION__VALUES =
        wrapEnumerationValues
            (new Object[] {
                 "BASIC",
                 "DIGEST",
                 "CLIENT-CERT",
                 "NONE"
             });

    /**
     * Validates the Enumeration constraint of '<em>Auth Method Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateAuthMethodType_Enumeration(String authMethodType, DiagnosticChain diagnostics, Map context) {
        boolean result = AUTH_METHOD_TYPE__ENUMERATION__VALUES.contains(authMethodType);
        if (!result && diagnostics != null) 
            reportEnumerationViolation(JarPackage.eINSTANCE.getAuthMethodType(), authMethodType, AUTH_METHOD_TYPE__ENUMERATION__VALUES, diagnostics, context);
        return result; 
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateTransportGuaranteeTypeObject(TransportGuaranteeType transportGuaranteeTypeObject, DiagnosticChain diagnostics, Map context) {
        return true;
    }

} //JarValidator
