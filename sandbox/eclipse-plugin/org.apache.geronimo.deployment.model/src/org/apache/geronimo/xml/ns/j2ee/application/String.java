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
package org.apache.geronimo.xml.ns.j2ee.application;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>String</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * 
 *    Mirrors j2ee:string.
 *             
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.String#getValue <em>Value</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.j2ee.application.String#getId <em>Id</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getString()
 * @model extendedMetaData="name='string' kind='simple'"
 * @generated
 */
public interface String extends EObject {
    /**
     * Returns the value of the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Value</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Value</em>' attribute.
     * @see #setValue(java.lang.String)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getString_Value()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.Token"
     *        extendedMetaData="name=':0' kind='simple'"
     * @generated
     */
    java.lang.String getValue();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.String#getValue <em>Value</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Value</em>' attribute.
     * @see #getValue()
     * @generated
     */
    void setValue(java.lang.String value);

    /**
     * Returns the value of the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Id</em>' attribute.
     * @see #setId(java.lang.String)
     * @see org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage#getString_Id()
     * @model unique="false" id="true" dataType="org.eclipse.emf.ecore.xml.type.ID"
     *        extendedMetaData="kind='attribute' name='id'"
     * @generated
     */
    java.lang.String getId();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.j2ee.application.String#getId <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Id</em>' attribute.
     * @see #getId()
     * @generated
     */
    void setId(java.lang.String value);

} // String
