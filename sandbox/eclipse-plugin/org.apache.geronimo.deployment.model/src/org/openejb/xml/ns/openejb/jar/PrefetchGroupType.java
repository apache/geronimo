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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Prefetch Group Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getGroup <em>Group</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getEntityGroupMapping <em>Entity Group Mapping</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getCmpFieldGroupMapping <em>Cmp Field Group Mapping</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getCmrFieldGroupMapping <em>Cmr Field Group Mapping</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getPrefetchGroupType()
 * @model extendedMetaData="name='prefetch-group_._type' kind='elementOnly'"
 * @generated
 */
public interface PrefetchGroupType extends EObject {
    /**
     * Returns the value of the '<em><b>Group</b></em>' containment reference list.
     * The list contents are of type {@link org.openejb.xml.ns.openejb.jar.GroupType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Group</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Group</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getPrefetchGroupType_Group()
     * @model type="org.openejb.xml.ns.openejb.jar.GroupType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='group' namespace='##targetNamespace'"
     * @generated
     */
    EList getGroup();

    /**
     * Returns the value of the '<em><b>Entity Group Mapping</b></em>' containment reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Entity Group Mapping</em>' containment reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Entity Group Mapping</em>' containment reference.
     * @see #setEntityGroupMapping(EntityGroupMappingType)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getPrefetchGroupType_EntityGroupMapping()
     * @model containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='entity-group-mapping' namespace='##targetNamespace'"
     * @generated
     */
    EntityGroupMappingType getEntityGroupMapping();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.PrefetchGroupType#getEntityGroupMapping <em>Entity Group Mapping</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Entity Group Mapping</em>' containment reference.
     * @see #getEntityGroupMapping()
     * @generated
     */
    void setEntityGroupMapping(EntityGroupMappingType value);

    /**
     * Returns the value of the '<em><b>Cmp Field Group Mapping</b></em>' containment reference list.
     * The list contents are of type {@link org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Cmp Field Group Mapping</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Cmp Field Group Mapping</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getPrefetchGroupType_CmpFieldGroupMapping()
     * @model type="org.openejb.xml.ns.openejb.jar.CmpFieldGroupMappingType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='cmp-field-group-mapping' namespace='##targetNamespace'"
     * @generated
     */
    EList getCmpFieldGroupMapping();

    /**
     * Returns the value of the '<em><b>Cmr Field Group Mapping</b></em>' containment reference list.
     * The list contents are of type {@link org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Cmr Field Group Mapping</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Cmr Field Group Mapping</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getPrefetchGroupType_CmrFieldGroupMapping()
     * @model type="org.openejb.xml.ns.openejb.jar.CmrFieldGroupMappingType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='cmr-field-group-mapping' namespace='##targetNamespace'"
     * @generated
     */
    EList getCmrFieldGroupMapping();

} // PrefetchGroupType
