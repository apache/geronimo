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
 * A representation of the model object '<em><b>Distinguished Name Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#getDescription <em>Description</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#isDesignatedRunAs <em>Designated Run As</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDistinguishedNameType()
 * @model extendedMetaData="name='distinguishedNameType' kind='elementOnly'"
 * @generated
 */
public interface DistinguishedNameType extends EObject{
    /**
     * Returns the value of the '<em><b>Description</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.security.DescriptionType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Description</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Description</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDistinguishedNameType_Description()
     * @model type="org.apache.geronimo.xml.ns.security.DescriptionType" containment="true" resolveProxies="false"
     *        extendedMetaData="kind='element' name='description' namespace='##targetNamespace'"
     * @generated
     */
    EList getDescription();

    /**
     * Returns the value of the '<em><b>Designated Run As</b></em>' attribute.
     * The default value is <code>"false"</code>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * 
     *                     Set this attribute to "true" if this principal is to be
     *                     used as the run-as principal for this role.
     *                 
     * <!-- end-model-doc -->
     * @return the value of the '<em>Designated Run As</em>' attribute.
     * @see #isSetDesignatedRunAs()
     * @see #unsetDesignatedRunAs()
     * @see #setDesignatedRunAs(boolean)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDistinguishedNameType_DesignatedRunAs()
     * @model default="false" unique="false" unsettable="true" dataType="org.eclipse.emf.ecore.xml.type.Boolean"
     *        extendedMetaData="kind='attribute' name='designated-run-as'"
     * @generated
     */
    boolean isDesignatedRunAs();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#isDesignatedRunAs <em>Designated Run As</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Designated Run As</em>' attribute.
     * @see #isSetDesignatedRunAs()
     * @see #unsetDesignatedRunAs()
     * @see #isDesignatedRunAs()
     * @generated
     */
    void setDesignatedRunAs(boolean value);

    /**
     * Unsets the value of the '{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#isDesignatedRunAs <em>Designated Run As</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #isSetDesignatedRunAs()
     * @see #isDesignatedRunAs()
     * @see #setDesignatedRunAs(boolean)
     * @generated
     */
    void unsetDesignatedRunAs();

    /**
     * Returns whether the value of the '{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#isDesignatedRunAs <em>Designated Run As</em>}' attribute is set.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return whether the value of the '<em>Designated Run As</em>' attribute is set.
     * @see #unsetDesignatedRunAs()
     * @see #isDesignatedRunAs()
     * @see #setDesignatedRunAs(boolean)
     * @generated
     */
    boolean isSetDesignatedRunAs();

    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see org.apache.geronimo.xml.ns.security.SecurityPackage#getDistinguishedNameType_Name()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='attribute' name='name'"
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.security.DistinguishedNameType#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

} // DistinguishedNameType
