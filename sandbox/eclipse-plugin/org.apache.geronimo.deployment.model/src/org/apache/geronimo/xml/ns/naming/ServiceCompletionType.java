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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Service Completion Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ServiceCompletionType#getServiceName <em>Service Name</em>}</li>
 *   <li>{@link org.apache.geronimo.xml.ns.naming.ServiceCompletionType#getPort <em>Port</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getServiceCompletionType()
 * @model extendedMetaData="name='service-completionType' kind='elementOnly'"
 * @generated
 */
public interface ServiceCompletionType extends EObject{
    /**
     * Returns the value of the '<em><b>Service Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Service Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Service Name</em>' attribute.
     * @see #setServiceName(String)
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getServiceCompletionType_ServiceName()
     * @model unique="false" dataType="org.eclipse.emf.ecore.xml.type.String" required="true"
     *        extendedMetaData="kind='element' name='service-name' namespace='##targetNamespace'"
     * @generated
     */
    String getServiceName();

    /**
     * Sets the value of the '{@link org.apache.geronimo.xml.ns.naming.ServiceCompletionType#getServiceName <em>Service Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Service Name</em>' attribute.
     * @see #getServiceName()
     * @generated
     */
    void setServiceName(String value);

    /**
     * Returns the value of the '<em><b>Port</b></em>' containment reference list.
     * The list contents are of type {@link org.apache.geronimo.xml.ns.naming.PortCompletionType}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Port</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Port</em>' containment reference list.
     * @see org.apache.geronimo.xml.ns.naming.NamingPackage#getServiceCompletionType_Port()
     * @model type="org.apache.geronimo.xml.ns.naming.PortCompletionType" containment="true" resolveProxies="false" required="true"
     *        extendedMetaData="kind='element' name='port' namespace='##targetNamespace'"
     * @generated
     */
    EList getPort();

} // ServiceCompletionType
