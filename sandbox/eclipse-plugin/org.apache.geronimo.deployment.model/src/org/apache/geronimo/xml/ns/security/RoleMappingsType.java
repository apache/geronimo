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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Role Mappings Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.RoleMappingsType#getRole <em>Role</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRoleMappingsType()
 * @model extendedMetaData="name='role-mappingsType' kind='elementOnly'"
 * @generated
 */
public interface RoleMappingsType extends EObject {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * Returns the value of the '<em><b>Role</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.security.RoleType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Role</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Role</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getRoleMappingsType_Role()
     * @model type="org.apache.geronimo.xml.ns.security.RoleType" containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='role' namespace='##targetNamespace'"
     * @generated
     */
    EList getRole();

} // RoleMappingsType
