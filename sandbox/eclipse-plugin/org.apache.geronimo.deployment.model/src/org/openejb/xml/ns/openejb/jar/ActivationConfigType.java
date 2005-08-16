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
 * A representation of the model object '<em><b>Activation Config Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.ActivationConfigType#getDescription <em>Description</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.ActivationConfigType#getActivationConfigProperty <em>Activation Config Property</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getActivationConfigType()
 * @model extendedMetaData="name='activation-configType' kind='elementOnly'"
 * @generated
 */
public interface ActivationConfigType extends EObject {
    /**
     * Returns the value of the '<em><b>Description</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Description</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Description</em>' attribute list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getActivationConfigType_Description()
     * @model type="java.lang.String" unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='element' name='description' namespace='##targetNamespace'"
     * @generated
     */
    EList getDescription();

    /**
     * Returns the value of the '<em><b>Activation Config Property</b></em>' containment reference list.
     * The list contents are of type {@link org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Activation Config Property</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Activation Config Property</em>' containment reference list.
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getActivationConfigType_ActivationConfigProperty()
     * @model type="org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType" containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='activation-config-property' namespace='##targetNamespace'"
     * @generated
     */
    EList getActivationConfigProperty();

} // ActivationConfigType
