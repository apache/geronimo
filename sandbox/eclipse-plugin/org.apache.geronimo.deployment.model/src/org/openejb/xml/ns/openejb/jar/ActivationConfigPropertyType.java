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

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Activation Config Property Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType#getActivationConfigPropertyName <em>Activation Config Property Name</em>}</li>
 *   <li>{@link org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType#getActivationConfigPropertyValue <em>Activation Config Property Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.openejb.xml.ns.openejb.jar.JarPackage#getActivationConfigPropertyType()
 * @model extendedMetaData="name='activation-config-propertyType' kind='elementOnly'"
 * @generated
 */
public interface ActivationConfigPropertyType extends EObject {
    /**
     * Returns the value of the '<em><b>Activation Config Property Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Activation Config Property Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Activation Config Property Name</em>' attribute.
     * @see #setActivationConfigPropertyName(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getActivationConfigPropertyType_ActivationConfigPropertyName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='activation-config-property-name' namespace='##targetNamespace'"
     * @generated
     */
    String getActivationConfigPropertyName();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType#getActivationConfigPropertyName <em>Activation Config Property Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Activation Config Property Name</em>' attribute.
     * @see #getActivationConfigPropertyName()
     * @generated
     */
    void setActivationConfigPropertyName(String value);

    /**
     * Returns the value of the '<em><b>Activation Config Property Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Activation Config Property Value</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Activation Config Property Value</em>' attribute.
     * @see #setActivationConfigPropertyValue(String)
     * @see org.openejb.xml.ns.openejb.jar.JarPackage#getActivationConfigPropertyType_ActivationConfigPropertyValue()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='activation-config-property-value' namespace='##targetNamespace'"
     * @generated
     */
    String getActivationConfigPropertyValue();

    /**
     * Sets the value of the '{@link org.openejb.xml.ns.openejb.jar.ActivationConfigPropertyType#getActivationConfigPropertyValue <em>Activation Config Property Value</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Activation Config Property Value</em>' attribute.
     * @see #getActivationConfigPropertyValue()
     * @generated
     */
    void setActivationConfigPropertyValue(String value);

} // ActivationConfigPropertyType
