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
package org.apache.geronimo.xml.ns.deployment;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Reference Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.deployment.ReferenceType#getName1 <em>Name1</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getReferenceType()
 * @model extendedMetaData="name='referenceType' kind='elementOnly'"
 * @generated
 */
public interface ReferenceType extends PatternType{
    /**
     * Returns the value of the '<em><b>Name1</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name1</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name1</em>' attribute.
     * @see #setName1(String)
     * @see org.apache.geronimo.xml.ns.deployment.DeploymentPackage#getReferenceType_Name1()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String"
     *        extendedMetaData="kind='attribute' name='name'"
     * @generated
     */
    String getName1();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.deployment.ReferenceType#getName1 <em>Name1</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name1</em>' attribute.
     * @see #getName1()
     * @generated
     */
    void setName1(String value);

} // ReferenceType
