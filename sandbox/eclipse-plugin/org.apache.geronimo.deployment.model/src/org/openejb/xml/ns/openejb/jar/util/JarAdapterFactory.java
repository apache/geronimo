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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.openejb.xml.ns.openejb.jar.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.openejb.xml.ns.openejb.jar.JarPackage
 * @generated
 */
public class JarAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static JarPackage modelPackage;

    /**
     * Creates an instance of the adapter factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public JarAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = JarPackage.eINSTANCE;
        }
    }

    /**
     * Returns whether this factory is applicable for the type of the object.
     * <!-- begin-user-doc -->
     * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
     * <!-- end-user-doc -->
     * @return whether this factory is applicable for the type of the object.
     * @generated
     */
    public boolean isFactoryForType(Object object) {
        if (object == modelPackage) {
            return true;
        }
        if (object instanceof EObject) {
            return ((EObject)object).eClass().getEPackage() == modelPackage;
        }
        return false;
    }

    /**
     * The switch the delegates to the <code>createXXX</code> methods.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected JarSwitch modelSwitch =
        new JarSwitch() {
            public Object caseActivationConfigPropertyType(ActivationConfigPropertyType object) {
                return createActivationConfigPropertyTypeAdapter();
            }
            public Object caseActivationConfigType(ActivationConfigType object) {
                return createActivationConfigTypeAdapter();
            }
            public Object caseCmpFieldGroupMappingType(CmpFieldGroupMappingType object) {
                return createCmpFieldGroupMappingTypeAdapter();
            }
            public Object caseCmpFieldMappingType(CmpFieldMappingType object) {
                return createCmpFieldMappingTypeAdapter();
            }
            public Object caseCmrFieldGroupMappingType(CmrFieldGroupMappingType object) {
                return createCmrFieldGroupMappingTypeAdapter();
            }
            public Object caseCmrFieldMappingType(CmrFieldMappingType object) {
                return createCmrFieldMappingTypeAdapter();
            }
            public Object caseCmrFieldType(CmrFieldType object) {
                return createCmrFieldTypeAdapter();
            }
            public Object caseCmrFieldType1(CmrFieldType1 object) {
                return createCmrFieldType1Adapter();
            }
            public Object caseDocumentRoot(DocumentRoot object) {
                return createDocumentRootAdapter();
            }
            public Object caseEjbRelationshipRoleType(EjbRelationshipRoleType object) {
                return createEjbRelationshipRoleTypeAdapter();
            }
            public Object caseEjbRelationType(EjbRelationType object) {
                return createEjbRelationTypeAdapter();
            }
            public Object caseEnterpriseBeansType(EnterpriseBeansType object) {
                return createEnterpriseBeansTypeAdapter();
            }
            public Object caseEntityBeanType(EntityBeanType object) {
                return createEntityBeanTypeAdapter();
            }
            public Object caseEntityGroupMappingType(EntityGroupMappingType object) {
                return createEntityGroupMappingTypeAdapter();
            }
            public Object caseGroupType(GroupType object) {
                return createGroupTypeAdapter();
            }
            public Object caseMessageDrivenBeanType(MessageDrivenBeanType object) {
                return createMessageDrivenBeanTypeAdapter();
            }
            public Object caseMethodParamsType(MethodParamsType object) {
                return createMethodParamsTypeAdapter();
            }
            public Object caseOpenejbJarType(OpenejbJarType object) {
                return createOpenejbJarTypeAdapter();
            }
            public Object casePrefetchGroupType(PrefetchGroupType object) {
                return createPrefetchGroupTypeAdapter();
            }
            public Object caseQueryMethodType(QueryMethodType object) {
                return createQueryMethodTypeAdapter();
            }
            public Object caseQueryType(QueryType object) {
                return createQueryTypeAdapter();
            }
            public Object caseRelationshipRoleSourceType(RelationshipRoleSourceType object) {
                return createRelationshipRoleSourceTypeAdapter();
            }
            public Object caseRelationshipsType(RelationshipsType object) {
                return createRelationshipsTypeAdapter();
            }
            public Object caseRoleMappingType(RoleMappingType object) {
                return createRoleMappingTypeAdapter();
            }
            public Object caseSessionBeanType(SessionBeanType object) {
                return createSessionBeanTypeAdapter();
            }
            public Object caseTssType(TssType object) {
                return createTssTypeAdapter();
            }
            public Object caseWebServiceSecurityType(WebServiceSecurityType object) {
                return createWebServiceSecurityTypeAdapter();
            }
            public Object defaultCase(EObject object) {
                return createEObjectAdapter();
            }
        };

    /**
     * Creates an adapter for the <code>target</code>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param target the object to adapt.
     * @return the adapter for the <code>target</code>.
     * @generated
     */
    public Adapter createAdapter(Notifier target) {
        return (Adapter)modelSwitch.doSwitch((EObject)target);
    }


    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType <em>Activation Config Property Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType
     * @generated
     */
    public Adapter createActivationConfigPropertyTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigType <em>Activation Config Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.ActivationConfigType
     * @generated
     */
    public Adapter createActivationConfigTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType <em>Cmp Field Group Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType
     * @generated
     */
    public Adapter createCmpFieldGroupMappingTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.CmpFieldMappingType <em>Cmp Field Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.CmpFieldMappingType
     * @generated
     */
    public Adapter createCmpFieldMappingTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType <em>Cmr Field Group Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType
     * @generated
     */
    public Adapter createCmrFieldGroupMappingTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.CmrFieldMappingType <em>Cmr Field Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldMappingType
     * @generated
     */
    public Adapter createCmrFieldMappingTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.CmrFieldType <em>Cmr Field Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldType
     * @generated
     */
    public Adapter createCmrFieldTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.CmrFieldType1 <em>Cmr Field Type1</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.CmrFieldType1
     * @generated
     */
    public Adapter createCmrFieldType1Adapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.DocumentRoot <em>Document Root</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.DocumentRoot
     * @generated
     */
    public Adapter createDocumentRootAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType <em>Ejb Relationship Role Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationshipRoleType
     * @generated
     */
    public Adapter createEjbRelationshipRoleTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.EjbRelationType <em>Ejb Relation Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.EjbRelationType
     * @generated
     */
    public Adapter createEjbRelationTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.EnterpriseBeansType <em>Enterprise Beans Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.EnterpriseBeansType
     * @generated
     */
    public Adapter createEnterpriseBeansTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.EntityBeanType <em>Entity Bean Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.EntityBeanType
     * @generated
     */
    public Adapter createEntityBeanTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.EntityGroupMappingType <em>Entity Group Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.EntityGroupMappingType
     * @generated
     */
    public Adapter createEntityGroupMappingTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.GroupType <em>Group Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.GroupType
     * @generated
     */
    public Adapter createGroupTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType <em>Message Driven Bean Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.MessageDrivenBeanType
     * @generated
     */
    public Adapter createMessageDrivenBeanTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.MethodParamsType <em>Method Params Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.MethodParamsType
     * @generated
     */
    public Adapter createMethodParamsTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.OpenejbJarType <em>Openejb Jar Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.OpenejbJarType
     * @generated
     */
    public Adapter createOpenejbJarTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType <em>Prefetch Group Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.PrefetchGroupType
     * @generated
     */
    public Adapter createPrefetchGroupTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.QueryMethodType <em>Query Method Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.QueryMethodType
     * @generated
     */
    public Adapter createQueryMethodTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.QueryType <em>Query Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.QueryType
     * @generated
     */
    public Adapter createQueryTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.RelationshipRoleSourceType <em>Relationship Role Source Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.RelationshipRoleSourceType
     * @generated
     */
    public Adapter createRelationshipRoleSourceTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.RelationshipsType <em>Relationships Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.RelationshipsType
     * @generated
     */
    public Adapter createRelationshipsTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.RoleMappingType <em>Role Mapping Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.RoleMappingType
     * @generated
     */
    public Adapter createRoleMappingTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.SessionBeanType <em>Session Bean Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.SessionBeanType
     * @generated
     */
    public Adapter createSessionBeanTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.TssType <em>Tss Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.TssType
     * @generated
     */
    public Adapter createTssTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link org.openejb.xml.ns.openejb.jar.WebServiceSecurityType <em>Web Service Security Type</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see org.openejb.xml.ns.openejb.jar.WebServiceSecurityType
     * @generated
     */
    public Adapter createWebServiceSecurityTypeAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for the default case.
     * <!-- begin-user-doc -->
     * This default implementation returns null.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @generated
     */
    public Adapter createEObjectAdapter() {
        return null;
    }

} //JarAdapterFactory
