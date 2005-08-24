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
 * A representation of the model object '<em><b>Role Mapping Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.RoleMappingType#getCmrFieldMapping <em>Cmr Field Mapping</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getRoleMappingType()
 * @model extendedMetaData="name='role-mapping_._type' kind='elementOnly'"
 * @generated
 */
public interface RoleMappingType extends EObject {
    /**
     * Returns the value of the '<em><b>Cmr Field Mapping</b></em>' containment reference list.
     * The list contents are of type {@link org.openejb.xml.ns.openejb.jar.CmrFieldMappingType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Cmr Field Mapping</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Cmr Field Mapping</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getRoleMappingType_CmrFieldMapping()
     * @model type="org.openejb.xml.ns.openejb.jar.CmrFieldMappingType" containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='cmr-field-mapping' namespace='##targetNamespace'"
     * @generated
     */
    EList getCmrFieldMapping();

} // RoleMappingType
