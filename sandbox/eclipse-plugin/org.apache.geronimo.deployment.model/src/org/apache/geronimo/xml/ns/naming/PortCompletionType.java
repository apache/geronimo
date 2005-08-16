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
package org.apache.geronimo.xml.ns.naming;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Port Completion Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.PortCompletionType#getBindingName <em>Binding Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getPortCompletionType()
 * @model extendedMetaData="name='port-completionType' kind='elementOnly'"
 * @generated
 */
public interface PortCompletionType extends PortType {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    String copyright = "Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.";

    /**
     * Returns the value of the '<em><b>Binding Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Binding Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Binding Name</em>' attribute.
     * @see #setBindingName(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getPortCompletionType_BindingName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='binding-name' namespace='##targetNamespace'"
     * @generated
     */
    String getBindingName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.PortCompletionType#getBindingName <em>Binding Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Binding Name</em>' attribute.
     * @see #getBindingName()
     * @generated
     */
    void setBindingName(String value);

} // PortCompletionType
